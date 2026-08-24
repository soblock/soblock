// -----------------------------------------------------------------------------
// SoBlock - game engine : the loop, the edits, the level of detail scheduler
// -----------------------------------------------------------------------------

var SELECTABLE = [Terran.MAN_BRICK, Terran.MAN_PARQUET, Terran.MAN_METAL,
	Terran.NAT_DIRT, Terran.NAT_GRASS, Terran.NAT_STONE];

var MAX_TARGET_J = 10;

var CHUNKS_PER_RADIUS2 = 96; // roughly how many chunks a radius of 1 costs, squared

// The detail budget follows the machine : while a frame costs us little and the
// player has asked for more detail than the budget can pay for, the budget
// grows, and it falls back as soon as frames get expensive or the heap fills.
var BUDGET_MIN = 300;
var BUDGET_CEILING = 8000;      // even a fast machine stops here
var CHUNK_HEAP_BYTES = 100000;  // measured : about 97 kB of heap per chunk
var BUDGET_EVERY_MS = 1000;     // the budget moves at most once a second
var BUDGET_STEP = 1.2;
var FRAME_CHEAP_MS = 6;         // our own work in a frame : room to spare
var FRAME_DEAR_MS = 12;         // too much of the frame is ours
var HEAP_ROOM = 0.5;            // never grow past this share of the heap limit
var HEAP_FULL = 0.75;
var ADAPT_EVERY_MS = 250;    // how often the detail radius may move
var ADAPT_EVERY_CALLS = 60;  // or this many frames, whichever comes first
var ADAPT_MAX_STEP = 0.06;   // and by how much, so at most about 24% a second

function Game(renderer) {
	this.renderer = renderer;
	this.player = new Player();
	this.walk = new PhysicsWalk();
	this.fly = new PhysicsFly();
	this.flying = false;
	this.targetJ = 0;
	this.targetContent = Terran.MAN_BRICK;
	// the keys set the booleans, a thumb stick sets the axes
	this.dirs = { forward: false, backward: false, left: false, right: false, up: false, down: false, axisX: 0, axisY: 0 };
	this.speedMult = 1;
	this.meshQueue = [];
	this.regenQueue = [];
	this.atoms = [];
	this.maxChunks = 1600;   // detail budget : how many meshed chunks we keep
	this.budgetCeiling = heapCeiling();
	this.targetRadius = 6;   // the level of detail the player asked for
	this.frameCosts = [];    // how long our own work took, over the last frames
	this.pending = null;     // batch of meshes being built
	this.stats = { splits: 0, merges: 0, lodMs: 0 };
	this.newWorld();
}

Game.prototype.newWorld = function () {
	this.wf = new WorldFunction(POW2[JMAX - 1], 50);
	this.modif = new ModifNode(0, 0, 0, JMAX, -1, 0, 0);
	this.builder = new Builder(this.wf, this.modif);
	this.setChunkBudget(this.maxChunks);
	this.root = new Node(0, 0, 0, JMAX, null);
	initChunk(this.builder, this.root);
	this.renderer.dropAll();
	this.meshQueue = [this.root];
	this.regenQueue = [];
	this.pending = null;
	this.atoms = [];
	this.nextLodState = GRAND_FATHER;
	this.player.x = POW2[JMAX - 1];
	this.player.y = POW2[JMAX - 1];
	this.player.z = POW2[JMAX - 1] + 100;
	this.player.theta = 0;
	this.player.phi = -0.3;
	this.player.vx = this.player.vy = this.player.vz = 0;
	this.walk.reset();
};

// --- level of detail ----------------------------------------------------------
// Work is cut into small steps so that a frame is never blocked : a step either
// splits or merges one chunk of the tree, or builds the mesh of one chunk.
// Meshes are uploaded (and the ones they replace dropped) in one go, so the
// player never sees a hole or two levels of detail fighting for the same pixels.

/** start a batch : build these meshes, then swap them in and drop those */
Game.prototype.beginBatch = function (build, drop) {
	this.pending = { build: build, built: [], drop: drop || [] };
};

Game.prototype.stepBatch = function () {
	var batch = this.pending;
	if (batch.build.length) {
		var chunk = batch.build.pop();
		if (chunk.state === GRAND_FATHER) {
			batch.built.push([chunk, buildChunkMesh(this.root, chunk, this.builder)]);
		}
		return;
	}
	for (var i = 0; i < batch.drop.length; i++) { this.renderer.dropChunk(batch.drop[i]); }
	for (var k = 0; k < batch.built.length; k++) {
		this.renderer.uploadChunk(batch.built[k][0], batch.built[k][1]);
	}
	this.pending = null;
};

/** one unit of background work, returns false when there is nothing left to do */
Game.prototype.step = function () {
	if (this.pending) { this.stepBatch(); return true; }
	if (this.regenQueue.length) { return this.regenJob(); }
	if (this.meshQueue.length) {
		this.beginBatch([this.meshQueue.pop()], []);
		return true;
	}
	return this.lodJob();
};

/**
 * A chunk hides the faces it shares with a solid neighbour, so when that
 * neighbour is cut finer or coarser those faces have to be worked out again.
 * The neighbours are rebuilt in the same batch as the change, so the swap is
 * atomic : without this the seam keeps the decisions of the level of detail it
 * was built against, and the player sees through the ground where they meet.
 */
Game.prototype.withNeighbours = function (node, build) {
	var list = neighbourChunks(this.root, node, []);
	for (var i = 0; i < list.length; i++) {
		if (build.indexOf(list[i]) < 0) { build.push(list[i]); }
	}
	return build;
};

/** split the most urgent chunk, or merge the most useless one */
Game.prototype.lodJob = function () {
	var first = this.nextLodState;
	this.nextLodState = (first === GRAND_FATHER) ? PATRIARCH : GRAND_FATHER;
	// try the chosen job, and the other one when there is nothing to do
	return this.tryLodJob(first) || this.tryLodJob(this.nextLodState);
};

Game.prototype.tryLodJob = function (state) {
	var p = this.player;
	var node = argMaxPriority(this.root, state, this.builder, p.x, p.y, p.z);
	if (!node) { return false; }
	if (state === GRAND_FATHER) {
		splitAllLeaf(this.builder, node);
		var build = [];
		for (var o = 0; o < 8; o++) {
			if (node.sons[o].state === GRAND_FATHER) { build.push(node.sons[o]); }
		}
		this.beginBatch(this.withNeighbours(node, build), [node]);
		this.stats.splits++;
	} else {
		var descendants = node.collectChunks([]);
		mergeAllLeaf(node);
		this.beginBatch(this.withNeighbours(node, [node]), descendants);
		this.stats.merges++;
	}
	return true;
};

/** regenerate a chunk whose content changed because of an edit */
Game.prototype.regenJob = function () {
	while (this.regenQueue.length) {
		var chunk = this.regenQueue.pop();
		if (!chunk || (chunk.state !== GRAND_FATHER && chunk.state !== PATRIARCH)) { continue; }
		// An edit queues the chunk of the block and of its neighbours, and one
		// of those can be an ancestor of another. Regenerating the ancestor
		// replaces the subtree, so the descendant is no longer in the world :
		// rebuilding it here would leave its faces on screen for good. Its
		// ground was regenerated with the ancestor anyway.
		if (this.root.smallestCellContaining(chunk.x, chunk.y, chunk.z, chunk.J) !== chunk) { continue; }
		var descendants = chunk.collectChunks([]);
		initChunk(this.builder, chunk);
		this.beginBatch(this.withNeighbours(chunk, [chunk]), descendants);
		return true;
	}
	return false;
};

/** the budget the device can afford, and the radius that roughly fits in it */
Game.prototype.setChunkBudget = function (chunks) {
	this.maxChunks = chunks;
	this.builder.radius = Math.max(1, Math.min(this.targetRadius, Math.sqrt(chunks / CHUNKS_PER_RADIUS2)));
	this.lastAdapt = 0;
	this.sinceAdapt = ADAPT_EVERY_CALLS;
	this.lastChunkCount = undefined;
};

/** how long the last frame took us, measured around our own work */
Game.prototype.observeFrame = function (ms) {
	this.frameCosts.push(ms);
	if (this.frameCosts.length > 90) { this.frameCosts.shift(); }
};

/** the middle of the recent frame costs, or null while we have too few */
Game.prototype.frameCost = function () {
	if (this.frameCosts.length < 20) { return null; }
	var sorted = this.frameCosts.slice().sort(function (a, b) { return a - b; });
	return sorted[sorted.length >> 1];
};

/** as many chunks as the heap can hold, never more than the flat ceiling */
function heapCeiling() {
	var m = (typeof performance !== 'undefined') && performance.memory;
	if (!m || !m.jsHeapSizeLimit) { return BUDGET_CEILING; }
	return Math.max(BUDGET_MIN, Math.min(BUDGET_CEILING,
		Math.floor(HEAP_ROOM * m.jsHeapSizeLimit / CHUNK_HEAP_BYTES)));
}

function heapShare() {
	var m = (typeof performance !== 'undefined') && performance.memory;
	if (!m || !m.jsHeapSizeLimit) { return 0; }
	return m.usedJSHeapSize / m.jsHeapSizeLimit;
}

/**
 * Spend as much detail as the machine will carry. The radius alone cannot do
 * this : it is capped by the budget, so on a fast machine it would sit well
 * below what the player asked for while most of the card went unused.
 */
Game.prototype.autoBudget = function () {
	var now = performance.now();
	if (now - (this.lastBudget || 0) < BUDGET_EVERY_MS) { return; }
	this.lastBudget = now;
	var cost = this.frameCost();
	if (cost === null) { return; }
	var heap = heapShare();
	if (cost > FRAME_DEAR_MS || heap > HEAP_FULL) {
		this.maxChunks = Math.max(BUDGET_MIN, Math.round(this.maxChunks / BUDGET_STEP));
		return;
	}
	// only worth growing when the budget is what holds the detail back
	if (cost >= FRAME_CHEAP_MS || heap > HEAP_ROOM) { return; }
	if (this.builder.radius >= this.targetRadius - 0.05) { return; }
	if (this.renderer.chunks.size < this.maxChunks * 0.9) { return; }
	if (this.pending || this.regenQueue.length || this.meshQueue.length) { return; }
	this.maxChunks = Math.min(this.budgetCeiling, Math.round(this.maxChunks * BUDGET_STEP));
};

/**
 * Keep the number of chunks near the budget by moving the level of detail
 * radius, rather than by refusing to split. A hard stop would freeze the whole
 * level of detail once the budget was full : nothing could be refined ahead of
 * the player because nothing far behind had crossed its merge threshold yet.
 * Pulling the radius keeps the priorities coherent, and it always leaves the
 * most deserving chunk splittable.
 */
Game.prototype.adaptDetail = function () {
	// The chunk count answers a change of radius only after many jobs, so step
	// on a timer rather than every frame : tied to the frame rate it would
	// adapt at different speeds on different machines, and wind far past the
	// budget while waiting for the count to catch up.
	var now = performance.now();
	this.sinceAdapt = (this.sinceAdapt || 0) + 1;
	// wall time normally comes first ; the call count keeps it moving when
	// frames are cheap, as they are once the world has settled
	if (now - (this.lastAdapt || 0) < ADAPT_EVERY_MS && this.sinceAdapt < ADAPT_EVERY_CALLS) { return; }
	this.lastAdapt = now;
	this.sinceAdapt = 0;
	var chunks = this.renderer.chunks.size;
	var radius = this.builder.radius;
	var budget = this.maxChunks;
	var previous = (this.lastChunkCount === undefined) ? chunks : this.lastChunkCount;
	this.lastChunkCount = chunks;
	var tooMany = chunks > budget * 1.05;
	var tooFew = chunks < budget * 0.9;
	if (!tooMany && !tooFew) { return; }
	if (tooFew && radius >= this.targetRadius) { return; }
	// merging and splitting take many jobs to answer a change of radius : while
	// the count is already moving the right way, wait rather than wind further
	if (tooMany && chunks < previous - 2) { return; }
	if (tooFew && chunks > previous + 2) { return; }
	// the number of chunks grows about as the square of the radius, so aim
	// straight at the budget, damped and rate limited to stay stable
	var factor = Math.pow(budget / Math.max(1, chunks), 0.125);
	factor = Math.max(1 - ADAPT_MAX_STEP, Math.min(1 + ADAPT_MAX_STEP, factor));
	this.builder.radius = Math.max(1, Math.min(this.targetRadius, radius * factor));
};

/** run background work for at most budget milliseconds */
Game.prototype.runJobs = function (budgetMs) {
	this.autoBudget();
	this.adaptDetail();
	var t0 = performance.now();
	var did = false;
	do {
		if (!this.step()) { break; }
		did = true;
	} while (performance.now() - t0 < budgetMs);
	this.stats.lodMs = performance.now() - t0;
	return did;
};

/** true while chunks are still missing detail around the player */
Game.prototype.needsRefinement = function () {
	if (this.meshQueue.length || this.regenQueue.length || this.pending) { return true; }
	if (this.renderer.chunks.size >= this.maxChunks * 0.9) { return false; }
	var p = this.player;
	return argMaxPriority(this.root, GRAND_FATHER, this.builder, p.x, p.y, p.z) !== null;
};

// --- edits --------------------------------------------------------------------

function neighbours6(b) {
	var max = POW2[JMAX - b.J];
	var list = [];
	var d = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]];
	for (var i = 0; i < d.length; i++) {
		var x = b.x + d[i][0], y = b.y + d[i][1], z = b.z + d[i][2];
		if (x >= 0 && y >= 0 && z >= 0 && x < max && y < max && z < max) { list.push({ x: x, y: y, z: z, J: b.J }); }
	}
	return list;
}

function neighbours18(b) {
	var max = POW2[JMAX - b.J];
	var list = [];
	for (var dx = -1; dx <= 1; dx++) {
		for (var dy = -1; dy <= 1; dy++) {
			for (var dz = -1; dz <= 1; dz++) {
				if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 2 || (dx === 0 && dy === 0 && dz === 0)) { continue; }
				var x = b.x + dx, y = b.y + dy, z = b.z + dz;
				if (x >= 0 && y >= 0 && z >= 0 && x < max && y < max && z < max) { list.push({ x: x, y: y, z: z, J: b.J }); }
			}
		}
	}
	return list;
}

Game.prototype.queueRegen = function (block, neighbours) {
	var chunk = this.root.smallestChunkContaining(block.x, block.y, block.z, block.J);
	if (chunk && this.regenQueue.indexOf(chunk) < 0) { this.regenQueue.push(chunk); }
	for (var i = 0; i < neighbours.length; i++) {
		var n = neighbours[i];
		var c = this.root.smallestChunkContaining(n.x, n.y, n.z, n.J);
		if (c && this.regenQueue.indexOf(c) < 0) { this.regenQueue.push(c); }
	}
};

Game.prototype.applyAtom = function (block, content, add) {
	var value = 1E16 * POW2[1 + JMAX - block.J];
	this.modif.addModif(block.x, block.y, block.z, block.J, add ? -value : value, add ? content : -1);
	this.modif.computeBounds();
	this.modif.computeSumAncestors();
};

Game.prototype.addBlock = function (block, content) {
	this.applyAtom(block, content, true);
	this.atoms.push([block.x, block.y, block.z, block.J, content, 1]);
	this.queueRegen(block, neighbours6(block));
};

Game.prototype.removeBlock = function (block) {
	this.applyAtom(block, -1, false);
	this.atoms.push([block.x, block.y, block.z, block.J, -1, 0]);
	this.queueRegen(block, neighbours18(block));
};

/** rebuild a whole world from a list of edits (loading a saved game) */
Game.prototype.loadAtoms = function (atoms) {
	this.newWorld();
	for (var i = 0; i < atoms.length; i++) {
		var a = atoms[i];
		var block = { x: a[0], y: a[1], z: a[2], J: a[3] };
		this.applyAtom(block, a[4], a[5] === 1);
		this.atoms.push(a.slice());
	}
	// the tree was built before the edits were known : start over
	this.root = new Node(0, 0, 0, JMAX, null);
	initChunk(this.builder, this.root);
	this.renderer.dropAll();
	this.meshQueue = [this.root];
	this.pending = null;
};

// --- update -------------------------------------------------------------------

Game.prototype.pick = function () {
	return pickBlocks(this.root, this.player, this.targetJ);
};

Game.prototype.update = function (dt) {
	dt = Math.min(dt, 100);
	if (this.flying) {
		this.fly.move(this.player, dt, this.dirs, this.speedMult, this.root);
	} else {
		this.walk.move(this.player, dt, this.dirs, this.speedMult, this.root);
	}
	this.runJobs(this.jobBudget || 6);
};

Game.prototype.setFlying = function (flying) {
	this.flying = flying;
	if (flying) {
		this.player.vx = this.player.vy = this.player.vz = 0;
	} else {
		this.walk.reset();
	}
};

Game.prototype.changeTargetJ = function (delta) {
	this.targetJ = Math.min(MAX_TARGET_J, Math.max(0, this.targetJ + delta));
};

Game.prototype.serialize = function (name) {
	return {
		v: 1,
		name: name,
		date: Date.now(),
		player: {
			x: this.player.x, y: this.player.y, z: this.player.z,
			theta: this.player.theta, phi: this.player.phi
		},
		flying: this.flying,
		atoms: this.atoms
	};
};

Game.prototype.deserialize = function (save) {
	this.loadAtoms(save.atoms || []);
	if (save.player) {
		this.player.x = save.player.x; this.player.y = save.player.y; this.player.z = save.player.z;
		this.player.theta = save.player.theta;
		// looking exactly along the vertical would make the camera degenerate
		var lim = Math.PI / 2 - 0.01;
		this.player.phi = Math.max(-lim, Math.min(lim, save.player.phi || 0));
	}
	this.setFlying(!!save.flying);
};

if (typeof module !== 'undefined') {
	module.exports = { Game: Game, SELECTABLE: SELECTABLE };
}

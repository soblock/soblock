// -----------------------------------------------------------------------------
// SoBlock - game engine : the loop, the edits, the level of detail scheduler
// -----------------------------------------------------------------------------

var SELECTABLE = [Terran.MAN_BRICK, Terran.MAN_PARQUET, Terran.MAN_METAL,
	Terran.NAT_DIRT, Terran.NAT_GRASS, Terran.NAT_STONE];

var MAX_TARGET_J = 10;

function Game(renderer) {
	this.renderer = renderer;
	this.player = new Player();
	this.walk = new PhysicsWalk();
	this.fly = new PhysicsFly();
	this.flying = false;
	this.targetJ = 0;
	this.targetContent = Terran.MAN_BRICK;
	this.dirs = { forward: false, backward: false, left: false, right: false, up: false, down: false };
	this.speedMult = 1;
	this.meshQueue = [];
	this.regenQueue = [];
	this.atoms = [];
	this.maxChunks = 1100;   // detail budget : how many meshed chunks we keep
	this.pending = null;     // batch of meshes being built
	this.stats = { splits: 0, merges: 0, lodMs: 0 };
	this.newWorld();
}

Game.prototype.newWorld = function () {
	this.wf = new WorldFunction(POW2[JMAX - 1], 50);
	this.modif = new ModifNode(0, 0, 0, JMAX, -1, 0, 0);
	this.builder = new Builder(this.wf, this.modif);
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

/** split the most urgent chunk, or merge the most useless one */
Game.prototype.lodJob = function () {
	var first = this.nextLodState;
	this.nextLodState = (first === GRAND_FATHER) ? PATRIARCH : GRAND_FATHER;
	// try the chosen job, and the other one when there is nothing to do
	return this.tryLodJob(first) || this.tryLodJob(this.nextLodState);
};

Game.prototype.tryLodJob = function (state) {
	var p = this.player;
	if (state === GRAND_FATHER && this.renderer.chunks.size >= this.maxChunks) { return false; }
	var node = argMaxPriority(this.root, state, this.builder, p.x, p.y, p.z);
	if (!node) { return false; }
	if (state === GRAND_FATHER) {
		splitAllLeaf(this.builder, node);
		var build = [];
		for (var o = 0; o < 8; o++) {
			if (node.sons[o].state === GRAND_FATHER) { build.push(node.sons[o]); }
		}
		this.beginBatch(build, [node]);
		this.stats.splits++;
	} else {
		var descendants = node.collectChunks([]);
		mergeAllLeaf(node);
		this.beginBatch([node], descendants);
		this.stats.merges++;
	}
	return true;
};

/** regenerate a chunk whose content changed because of an edit */
Game.prototype.regenJob = function () {
	while (this.regenQueue.length) {
		var chunk = this.regenQueue.pop();
		if (!chunk || (chunk.state !== GRAND_FATHER && chunk.state !== PATRIARCH)) { continue; }
		var descendants = chunk.collectChunks([]);
		initChunk(this.builder, chunk);
		this.beginBatch([chunk], descendants);
		return true;
	}
	return false;
};

/** run background work for at most budget milliseconds */
Game.prototype.runJobs = function (budgetMs) {
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
	if (this.renderer.chunks.size >= this.maxChunks) { return false; }
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
		this.player.theta = save.player.theta; this.player.phi = save.player.phi;
	}
	this.setFlying(!!save.flying);
};

if (typeof module !== 'undefined') {
	module.exports = { Game: Game, SELECTABLE: SELECTABLE };
}

// -----------------------------------------------------------------------------
// SoBlock - sparse voxel octree ("blocktree") with level of detail
// Port of org.wavecraft.geometry.blocktree.* and .modif.*
// -----------------------------------------------------------------------------

var JMAX = 12;                 // the world is a cube of 2^JMAX = 4096 units
var BLOCK_LOG_SIZE = 3;        // a mesh chunk holds 2^(BLOCK_LOG_SIZE+1) leaves per axis

// node states (Blocktree.State)
var PATRIARCH = 0;    // subdivided into finer chunks
var GRAND_FATHER = 1; // root of a mesh chunk
var FATHER = 2;       // inner node of a chunk
var DEAD_GROUND = 3;  // fully underground, no geometry
var DEAD_AIR = 4;     // fully in the air
var LEAF = 5;         // a visible block

var POW2 = new Float64Array(64);
for (var _i = 0; _i < 64; _i++) { POW2[_i] = Math.pow(2, _i); }

function ithbit(k, i) {
	var ttjm = POW2[i - 1];
	return ((k % (2 * ttjm)) / ttjm) | 0;
}

function divideFloor(a, b) { return Math.floor(a / b); }

// --- blocks -------------------------------------------------------------------
// A dyadic block is (x, y, z, J) : the cube [x,x+1[ x [y,y+1[ x [z,z+1[ scaled by 2^J.

function blockContains(ax, ay, az, aJ, bx, by, bz, bJ) {
	if (aJ < bJ) { return false; }
	var step = POW2[aJ - bJ];
	return divideFloor(bx, step) === ax && divideFloor(by, step) === ay && divideFloor(bz, step) === az;
}

function Node(x, y, z, J, father) {
	this.x = x; this.y = y; this.z = z; this.J = J;
	this.state = DEAD_GROUND;
	this.sons = null;
	this.father = father || null;
	this.content = -1;
	this.mesh = null;     // set on GRAND_FATHER nodes that own a mesh
	this.meshDirty = false;
}

Node.prototype.edge = function () { return POW2[this.J]; };
Node.prototype.cx = function () { return (this.x + 0.5) * POW2[this.J]; };
Node.prototype.cy = function () { return (this.y + 0.5) * POW2[this.J]; };
Node.prototype.cz = function () { return (this.z + 0.5) * POW2[this.J]; };

Node.prototype.distance = function (px, py, pz) {
	var t = POW2[this.J];
	var dx = t * (this.x + 0.5) - px, dy = t * (this.y + 0.5) - py, dz = t * (this.z + 0.5) - pz;
	return Math.sqrt(dx * dx + dy * dy + dz * dz);
};

Node.prototype.initSons = function () {
	var sons = new Array(8);
	var x2 = 2 * this.x, y2 = 2 * this.y, z2 = 2 * this.z, J = this.J - 1;
	for (var o = 0; o < 8; o++) {
		var s = new Node(x2 + (o & 1), y2 + ((o >> 1) & 1), z2 + ((o >> 2) & 1), J, this);
		s.content = this.content;
		sons[o] = s;
	}
	this.sons = sons;
};

Node.prototype.findSonContaining = function (bx, by, bz, bJ) {
	if (!blockContains(this.x, this.y, this.z, this.J, bx, by, bz, bJ)) { return -1; }
	var d = this.J - bJ;
	return ithbit(bx, d) + 2 * ithbit(by, d) + 4 * ithbit(bz, d);
};

/** smallest node of the tree containing the given block, or null */
Node.prototype.smallestCellContaining = function (bx, by, bz, bJ) {
	if (this.x === bx && this.y === by && this.z === bz && this.J === bJ) { return this; }
	if (blockContains(this.x, this.y, this.z, this.J, bx, by, bz, bJ)) {
		if (this.sons) {
			return this.sons[this.findSonContaining(bx, by, bz, bJ)].smallestCellContaining(bx, by, bz, bJ);
		}
		return this;
	}
	return null;
};

/** smallest chunk-owning ancestor containing a block : the mesh(es) to rebuild */
Node.prototype.smallestChunkContaining = function (bx, by, bz, bJ) {
	if (this.x === bx && this.y === by && this.z === bz && this.J === bJ) { return this; }
	if (!blockContains(this.x, this.y, this.z, this.J, bx, by, bz, bJ)) { return null; }
	if (this.state === PATRIARCH) {
		var son = this.sons[this.findSonContaining(bx, by, bz, bJ)];
		if (son.state === PATRIARCH || son.state === GRAND_FATHER) {
			return son.smallestChunkContaining(bx, by, bz, bJ);
		}
	}
	return this;
};

/** all GRAND_FATHER descendants (chunks owning a mesh) */
Node.prototype.collectChunks = function (out) {
	if (this.state === GRAND_FATHER) { out.push(this); }
	if ((this.state === GRAND_FATHER || this.state === PATRIARCH) && this.sons) {
		for (var i = 0; i < 8; i++) { this.sons[i].collectChunks(out); }
	}
	return out;
};

/** the deepest nodes of a chunk (leaf level), used to seed the light cache */
Node.prototype.collectGreatChildren = function (out, rootJ) {
	if (this.J >= rootJ - BLOCK_LOG_SIZE) {
		if (this.sons) {
			for (var i = 0; i < 8; i++) { this.sons[i].collectGreatChildren(out, rootJ); }
		}
	} else if (this.J === rootJ - BLOCK_LOG_SIZE - 1) {
		out.push(this);
	}
	return out;
};

// --- modification octree ------------------------------------------------------
// Player edits are stored as a sparse octree of additive offsets applied to the
// world function. Port of ModifOctree.

function ModifNode(x, y, z, J, content, value, sumAncestors) {
	this.x = x; this.y = y; this.z = z; this.J = J;
	this.sons = null;
	this.father = null;
	this.content = (content === undefined) ? -1 : content;
	this.value = value || 0;
	this.sumAncestors = sumAncestors || 0;
	this.boundMin = 0;
	this.boundMax = 0;
}

ModifNode.prototype.findSonContainingBlock = function (bx, by, bz, bJ) {
	var d = this.J - bJ;
	return ithbit(bx, d) + 2 * ithbit(by, d) + 4 * ithbit(bz, d);
};

ModifNode.prototype.computeBounds = function () {
	this.boundMin = 0;
	this.boundMax = 0;
	if (this.sons) {
		for (var o = 0; o < 8; o++) {
			var s = this.sons[o];
			if (s) {
				s.computeBounds();
				this.boundMax = s.boundMax + s.value;
				this.boundMin = s.boundMin + s.value;
			}
		}
		for (var o2 = 0; o2 < 8; o2++) {
			var s2 = this.sons[o2];
			if (s2) {
				this.boundMax = Math.max(this.boundMax, s2.boundMax + s2.value);
				this.boundMin = Math.min(this.boundMin, s2.boundMin + s2.value);
			} else {
				this.boundMax = Math.max(this.boundMax, 0);
				this.boundMin = Math.min(this.boundMin, 0);
			}
		}
	}
};

ModifNode.prototype.computeSumAncestors = function () {
	if (this.father === null) { this.sumAncestors = 0; }
	if (this.sons) {
		for (var o = 0; o < 8; o++) {
			var s = this.sons[o];
			if (s) {
				s.sumAncestors = this.value + this.sumAncestors;
				s.computeSumAncestors();
			}
		}
	}
};

ModifNode.prototype.smallestCellContainingBlock = function (bx, by, bz, bJ) {
	if (this.J === bJ || !this.sons) { return this; }
	var o = this.findSonContainingBlock(bx, by, bz, bJ);
	if (o < 0 || !this.sons[o]) { return this; }
	return this.sons[o].smallestCellContainingBlock(bx, by, bz, bJ);
};

ModifNode.prototype.smallestNegativeCellContainingBlock = function (bx, by, bz, bJ) {
	var save = null, node = this;
	while (node) {
		if (node.value < 0) { save = node; }
		if (node.J === bJ || !node.sons) { break; }
		var o = node.findSonContainingBlock(bx, by, bz, bJ);
		if (o < 0 || !node.sons[o]) { break; }
		node = node.sons[o];
	}
	return save;
};

ModifNode.prototype.maxValueAtFrRoot = function (bx, by, bz, bJ) {
	var cell = this.smallestCellContainingBlock(bx, by, bz, bJ);
	if (!cell.sons) { return cell.sumAncestors + cell.value; }
	if (cell.J === bJ) { return cell.sumAncestors + cell.value + cell.boundMax; }
	return cell.sumAncestors + cell.value;
};

ModifNode.prototype.minValueAtFrRoot = function (bx, by, bz, bJ) {
	var cell = this.smallestCellContainingBlock(bx, by, bz, bJ);
	if (!cell.sons) { return cell.sumAncestors + cell.value; }
	if (cell.J === bJ) { return cell.sumAncestors + cell.value + cell.boundMin; }
	return cell.sumAncestors + cell.value;
};

/** biggest modification step between a block and its 6 neighbours */
ModifNode.prototype.jumpMax = function (bx, by, bz, bJ) {
	var sizeMax = POW2[JMAX - bJ];
	var jump = 0;
	var vMin = this.minValueAtFrRoot(bx, by, bz, bJ);
	if (bx + 1 < sizeMax) { jump = Math.max(jump, this.maxValueAtFrRoot(bx + 1, by, bz, bJ) - vMin); }
	if (bx - 1 >= 0) { jump = Math.max(jump, this.maxValueAtFrRoot(bx - 1, by, bz, bJ) - vMin); }
	if (by + 1 < sizeMax) { jump = Math.max(jump, this.maxValueAtFrRoot(bx, by + 1, bz, bJ) - vMin); }
	if (by - 1 >= 0) { jump = Math.max(jump, this.maxValueAtFrRoot(bx, by - 1, bz, bJ) - vMin); }
	if (bz + 1 < sizeMax) { jump = Math.max(jump, this.maxValueAtFrRoot(bx, by, bz + 1, bJ) - vMin); }
	if (bz - 1 >= 0) { jump = Math.max(jump, this.maxValueAtFrRoot(bx, by, bz - 1, bJ) - vMin); }
	return jump;
};

ModifNode.prototype.addModif = function (bx, by, bz, bJ, val, content) {
	var node = this;
	while (node.J > bJ) {
		var ind = node.findSonContainingBlock(bx, by, bz, bJ);
		if (!node.sons) { node.sons = new Array(8).fill(null); }
		var sumAncestorOfMySons = node.sumAncestors + node.value;
		var sx = 2 * node.x + (ind & 1), sy = 2 * node.y + ((ind >> 1) & 1), sz = 2 * node.z + ((ind >> 2) & 1);
		if (node.J === bJ + 1) {
			var son = new ModifNode(sx, sy, sz, node.J - 1, content, val - sumAncestorOfMySons, sumAncestorOfMySons);
			son.father = node;
			node.sons[ind] = son;
			return;
		}
		if (!node.sons[ind]) {
			var mid = new ModifNode(sx, sy, sz, node.J - 1, -1, 0, sumAncestorOfMySons);
			mid.father = node;
			node.sons[ind] = mid;
		}
		node = node.sons[ind];
	}
};

// --- builder ------------------------------------------------------------------
// Decides, for a given block, whether it is ground, air, or crossed by the
// surface. Port of BlocktreeBuilderThreeDimFunModif.

function Builder(worldFunction, modif) {
	this.wf = worldFunction;
	this.modif = modif;
	this.radius = 6; // level of detail radius, tweakable in game
}

Builder.prototype.minMaxAtVertices = function (x, y, z, J) {
	var t = POW2[J];
	var x0 = x * t, y0 = y * t, z0 = z * t, x1 = x0 + t, y1 = y0 + t, z1 = z0 + t;
	var wf = this.wf;
	var vMin = Infinity, vMax = -Infinity, v;
	v = wf.valueAt(x0, y0, z0); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x1, y0, z0); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x0, y1, z0); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x1, y1, z0); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x0, y0, z1); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x1, y0, z1); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x0, y1, z1); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	v = wf.valueAt(x1, y1, z1); if (v < vMin) vMin = v; if (v > vMax) vMax = v;
	this._vMin = vMin;
	this._vMax = vMax;
};

Builder.prototype.isGround = function (x, y, z, J) {
	var cell = this.modif.smallestCellContainingBlock(x, y, z, J);
	var S, bmax, v;
	if (cell.J > J && cell.sons) { S = cell.sumAncestors; v = cell.value; bmax = 0; }
	else { S = cell.sumAncestors; bmax = cell.boundMax; v = cell.value; }
	this.minMaxAtVertices(x, y, z, J);
	return (this._vMax + S + v + bmax) < 0;
};

Builder.prototype.isIntersectingSurface = function (x, y, z, J) {
	var cell = this.modif.smallestCellContainingBlock(x, y, z, J);
	var S, bmax, bmin, v;
	if (cell.J > J && cell.sons) { S = cell.sumAncestors; v = cell.value; bmin = 0; bmax = 0; }
	else { S = cell.sumAncestors; bmin = cell.boundMin; bmax = cell.boundMax; v = cell.value; }
	this.minMaxAtVertices(x, y, z, J);
	var vMin = this._vMin + S + v + bmin;
	var vMax = this._vMax + S + v + bmax;
	var jumpMax = this.modif.jumpMax(x, y, z, J);
	var dphi = this.wf.uncertaintyBound(J);
	return !(vMin > dphi || vMax + jumpMax < -dphi);
};

Builder.prototype.contentAt = function (x, y, z, J) {
	var m = this.modif.smallestNegativeCellContainingBlock(x, y, z, J);
	if (m && m.value < 0 && m.content >= 0) { return m.content; }
	var t = POW2[J];
	return this.wf.contentAt((x + 0.5) * t, (y + 0.5) * t, (z + 0.5) * t);
};

/** state of a block given the raw min/max of the world function at its corners */
Builder.prototype.classify = function (x, y, z, J, vMinRaw, vMaxRaw) {
	var cell = this.modif.smallestCellContainingBlock(x, y, z, J);
	var S = cell.sumAncestors, v = cell.value, bmin, bmax;
	if (cell.J > J && cell.sons) { bmin = 0; bmax = 0; }
	else { bmin = cell.boundMin; bmax = cell.boundMax; }
	var vMin = vMinRaw + S + v + bmin;
	var vMax = vMaxRaw + S + v + bmax;
	var dphi = this.wf.uncertaintyBound(J);
	if (vMin <= dphi) {
		if (vMax + this.modif.jumpMax(x, y, z, J) >= -dphi) { return LEAF; }
	}
	return (vMax < 0) ? DEAD_GROUND : DEAD_AIR;
};

/** level of detail priority : > 1 means "split me" (or "merge me" for chunks) */
Builder.prototype.priority = function (node, px, py, pz) {
	if (node.J <= BLOCK_LOG_SIZE + 1) { return 0; }
	var ratio = this.radius * POW2[node.J] / (node.distance(px, py, pz) + 0.01);
	if (node.state === GRAND_FATHER) { return ratio; }
	if (node.state === PATRIARCH) { return 1 / ratio; }
	return 0;
};

// --- tree updates -------------------------------------------------------------
// Port of BlocktreeUpdaterSimple : split a chunk into 8 finer chunks, or merge
// them back.

var CORNERS = new Float64Array(27);

/** LEAF -> FATHER : subdivide a visible block into 8 sons.
 *  The world function is sampled once on the 3x3x3 lattice shared by the sons. */
function splitLeaf(builder, leaf) {
	if (leaf.state !== LEAF) { return; }
	leaf.initSons();
	var J = leaf.J;
	var h = POW2[J - 1];
	var x0 = leaf.x * POW2[J], y0 = leaf.y * POW2[J], z0 = leaf.z * POW2[J];
	var wf = builder.wf;
	for (var k = 0; k < 3; k++) {
		for (var j = 0; j < 3; j++) {
			for (var i = 0; i < 3; i++) {
				CORNERS[i + 3 * j + 9 * k] = wf.valueAt(x0 + i * h, y0 + j * h, z0 + k * h);
			}
		}
	}
	for (var o = 0; o < 8; o++) {
		var s = leaf.sons[o];
		var bx = o & 1, by = (o >> 1) & 1, bz = (o >> 2) & 1;
		var base = bx + 3 * by + 9 * bz;
		var vMin = Infinity, vMax = -Infinity, c;
		for (var dk = 0; dk < 2; dk++) {
			for (var dj = 0; dj < 2; dj++) {
				c = CORNERS[base + 3 * dj + 9 * dk];
				if (c < vMin) { vMin = c; } if (c > vMax) { vMax = c; }
				c = CORNERS[base + 1 + 3 * dj + 9 * dk];
				if (c < vMin) { vMin = c; } if (c > vMax) { vMax = c; }
			}
		}
		s.state = builder.classify(s.x, s.y, s.z, s.J, vMin, vMax);
		if (s.state === LEAF) {
			s.content = builder.contentAt(s.x, s.y, s.z, s.J);
		}
	}
	leaf.state = FATHER;
}

function splitAllLeafInner(builder, node, chunkJ) {
	if (node.J >= chunkJ - BLOCK_LOG_SIZE) {
		if (node.sons) {
			for (var o = 0; o < 8; o++) { splitAllLeafInner(builder, node.sons[o], chunkJ); }
		}
	} else {
		splitLeaf(builder, node);
	}
}

function cleanDeadSons(node) {
	var air = 0, ground = 0;
	if (node.sons) {
		for (var o = 0; o < 8; o++) {
			var s = node.sons[o];
			cleanDeadSons(s);
			if (s.state === DEAD_AIR) { air++; }
			else if (s.state === DEAD_GROUND) { ground++; }
		}
	}
	if (air === 8) { node.sons = null; node.state = DEAD_AIR; }
	if (ground === 8) { node.sons = null; node.state = DEAD_GROUND; }
}

/** GRAND_FATHER -> PATRIARCH : one more level of detail in this chunk */
function splitAllLeaf(builder, chunk) {
	splitAllLeafInner(builder, chunk, chunk.J);
	chunk.state = PATRIARCH;
	for (var o = 0; o < 8; o++) {
		var son = chunk.sons[o];
		if (son.state === FATHER) { son.state = GRAND_FATHER; }
		cleanDeadSons(son); // collapse the fully solid / fully empty sub trees
	}
}

function mergeLeaf(node) {
	if (node.sons) { node.sons = null; node.state = LEAF; }
}

function mergeAllLeafInner(patriarch, node) {
	if (node.J >= patriarch.J - BLOCK_LOG_SIZE) {
		if (node.sons) {
			node.state = FATHER;
			for (var o = 0; o < 8; o++) { mergeAllLeafInner(patriarch, node.sons[o]); }
		}
	} else {
		mergeLeaf(node);
	}
}

/** PATRIARCH -> GRAND_FATHER : one level of detail less in this chunk */
function mergeAllLeaf(patriarch) {
	mergeAllLeafInner(patriarch, patriarch);
	patriarch.state = GRAND_FATHER;
	for (var o = 0; o < 8; o++) {
		if (patriarch.sons[o].state === GRAND_FATHER) { patriarch.sons[o].state = FATHER; }
	}
}

/** (re)build a chunk from scratch down to its leaf level (after an edit) */
function initChunk(builder, node) {
	node.state = LEAF;
	initChunkInner(builder, node, node.J);
	node.state = GRAND_FATHER;
	if (node.sons) {
		for (var o = 0; o < 8; o++) { cleanDeadSons(node.sons[o]); }
	}
}

function initChunkInner(builder, node, rootJ) {
	if (node.J >= rootJ - BLOCK_LOG_SIZE) {
		splitLeaf(builder, node);
		if (node.state === FATHER) {
			for (var o = 0; o < 8; o++) { initChunkInner(builder, node.sons[o], rootJ); }
		}
	}
}

/** the node of the given state with the highest priority, or null */
function argMaxPriority(root, state, builder, px, py, pz) {
	var best = null, bestValue = 0;
	var stack = [root];
	while (stack.length) {
		var node = stack.pop();
		if (node.state !== PATRIARCH && node.state !== GRAND_FATHER) { continue; }
		if (node.state === state) {
			var p = builder.priority(node, px, py, pz);
			if (p > bestValue) { bestValue = p; best = node; }
		}
		if (node.sons) {
			for (var o = 0; o < 8; o++) { stack.push(node.sons[o]); }
		}
	}
	if (best && bestValue <= 1) { return null; } // not worth splitting / merging yet
	return best;
}

if (typeof module !== 'undefined') {
	module.exports = {
		Node: Node, ModifNode: ModifNode, Builder: Builder, JMAX: JMAX, BLOCK_LOG_SIZE: BLOCK_LOG_SIZE,
		PATRIARCH: PATRIARCH, GRAND_FATHER: GRAND_FATHER, FATHER: FATHER, DEAD_AIR: DEAD_AIR,
		DEAD_GROUND: DEAD_GROUND, LEAF: LEAF, splitAllLeaf: splitAllLeaf, mergeAllLeaf: mergeAllLeaf,
		initChunk: initChunk, argMaxPriority: argMaxPriority, POW2: POW2, blockContains: blockContains
	};
}

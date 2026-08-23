// -----------------------------------------------------------------------------
// SoBlock - turning a chunk of the octree into a triangle mesh
// Port of BlocktreeRasterizer, LightFace, FaceToArray and MegaTexture
// -----------------------------------------------------------------------------

var ATLAS_TILES = 16;   // terrain4.png is a 16x16 grid of 16x16 pixels tiles
var ATLAS_SIZE = 256;
var ATLAS_EPS = 1.01;

/** [column, row] of the texture tile for a material and a face normal */
function metaTexCoord(terran, normal) {
	switch (terran) {
		case Terran.NAT_DIRT: return [2, 0];
		case Terran.NAT_GALET: return [0, 1];
		case Terran.NAT_GRASS:
			if (normal === 3) { return [5, 8]; }
			return (normal === -3) ? [2, 0] : [3, 0];
		case Terran.NAT_GROUNDICE:
			if (normal === 3) { return [2, 4]; }
			return (normal === -3) ? [2, 0] : [4, 4];
		case Terran.NAT_GROUNDSNOW:
			if (normal === 3) { return [3, 4]; }
			return (normal === -3) ? [2, 0] : [4, 4];
		case Terran.NAT_ICE: return [2, 4];
		case Terran.NAT_SAND: return [2, 1];
		case Terran.NAT_SOLIDSAND: return [0, 13];
		case Terran.NAT_STONE: return [1, 0];
		case Terran.NAT_HERBY_ROCK: return [4, 2];
		case Terran.MAN_BRICK: return [7, 0];
		case Terran.MAN_PARQUET: return [4, 0];
		case Terran.MAN_METAL: return [6, 0];
		default: return [0, 0];
	}
}

/** [uMin, uMax, vMin, vMax] in texture space */
function texCoord(terran, normal) {
	var mt = metaTexCoord(terran, normal);
	var col = mt[0], row = mt[1];
	return [
		(col * ATLAS_TILES + ATLAS_EPS) / ATLAS_SIZE,
		((col + 1) * ATLAS_TILES - ATLAS_EPS) / ATLAS_SIZE,
		((row + 1) * ATLAS_TILES - ATLAS_EPS) / ATLAS_SIZE,
		(row * ATLAS_TILES + ATLAS_EPS) / ATLAS_SIZE
	];
}

// the 4 corners of a face, in the order used by the original game, given as
// offsets (in units of the face edge) for each normal
var FACE_CORNERS = {
	1: [[0, 0, 0], [0, 1, 0], [0, 1, 1], [0, 0, 1]],
	'-1': [[0, 0, 0], [0, 0, 1], [0, 1, 1], [0, 1, 0]],
	2: [[0, 0, 0], [0, 0, 1], [1, 0, 1], [1, 0, 0]],
	'-2': [[0, 0, 0], [1, 0, 0], [1, 0, 1], [0, 0, 1]],
	3: [[0, 0, 0], [1, 0, 0], [1, 1, 0], [0, 1, 0]],
	'-3': [[0, 0, 0], [0, 1, 0], [1, 1, 0], [1, 0, 0]]
};

// texture coordinates of those 4 corners : [uIndex, vIndex] with 0 = min, 1 = max
var FACE_UV = {
	1: [[0, 0], [1, 0], [1, 1], [0, 1]],
	'-1': [[1, 0], [1, 1], [0, 1], [0, 0]],
	2: [[1, 0], [1, 1], [0, 1], [0, 0]],
	'-2': [[0, 0], [1, 0], [1, 1], [0, 1]],
	3: [[0, 0], [0, 1], [1, 1], [1, 0]],
	'-3': [[1, 0], [0, 0], [0, 1], [1, 1]]
};

// key of a block, for the hash maps below (coordinates fit in 13 bits, and may
// be -1 on the border of the world)
function blockKey(x, y, z) {
	return ((x + 1) * 8192 + (y + 1)) * 8192 + (z + 1);
}

function faceKey(x, y, z, normal) {
	return blockKey(x, y, z) * 8 + (normal + 3);
}

/** the four blocks touching a vertex on the side the face is looking at */
function inFrontOfVertice(fx, fy, fz, J, normal, corner, out) {
	// vertex position in block units at scale J
	var xb = fx + corner[0], yb = fy + corner[1], zb = fz + corner[2];
	var id1, id2;
	switch (normal) {
		case 1: case -1: xb = (normal > 0) ? xb : xb - 1; id1 = 2; id2 = 3; break;
		case 2: case -2: yb = (normal > 0) ? yb : yb - 1; id1 = 1; id2 = 3; break;
		default: zb = (normal > 0) ? zb : zb - 1; id1 = 1; id2 = 2; break;
	}
	out[0] = xb; out[1] = yb; out[2] = zb;
	out[3] = xb + (id1 === 1 ? -1 : 0); out[4] = yb + (id1 === 2 ? -1 : 0); out[5] = zb + (id1 === 3 ? -1 : 0);
	out[6] = xb + ((id1 === 1 || id2 === 1) ? -1 : 0);
	out[7] = yb + ((id1 === 2 || id2 === 2) ? -1 : 0);
	out[8] = zb + ((id1 === 3 || id2 === 3) ? -1 : 0);
	out[9] = xb + (id2 === 1 ? -1 : 0); out[10] = yb + (id2 === 2 ? -1 : 0); out[11] = zb + (id2 === 3 ? -1 : 0);
}

/** collect the visible faces of a chunk : all faces of its leaves, minus the
 *  ones shared with another block (inside the chunk or in a neighbour chunk) */
function collectFaces(root, chunk) {
	var faces = new Map();
	var stack = [chunk];
	while (stack.length) {
		var node = stack.pop();
		var st = node.state;
		if (st === FATHER || st === GRAND_FATHER || st === PATRIARCH) {
			for (var o = 0; o < 8; o++) { stack.push(node.sons[o]); }
		} else if (st === LEAF) {
			var x = node.x, y = node.y, z = node.z, J = node.J, c = node.content;
			addFace(faces, x, y, z, J, -1, c);
			addFace(faces, x, y, z, J, -2, c);
			addFace(faces, x, y, z, J, -3, c);
			addFace(faces, x + 1, y, z, J, 1, c);
			addFace(faces, x, y + 1, z, J, 2, c);
			addFace(faces, x, y, z + 1, J, 3, c);
		}
	}
	// second pass : drop the faces hidden by a block of a neighbour chunk
	var visible = [];
	faces.forEach(function (f) {
		var bx = f.x, by = f.y, bz = f.z;
		if (f.n === -1) { bx -= 1; } else if (f.n === -2) { by -= 1; } else if (f.n === -3) { bz -= 1; }
		var front = root.smallestCellContaining(bx, by, bz, f.J);
		if (front === null || (front.state !== LEAF && front.state !== DEAD_GROUND)) {
			visible.push(f);
		}
	});
	return visible;
}

function addFace(faces, x, y, z, J, normal, content) {
	var reverse = faceKey(x, y, z, -normal);
	if (faces.has(reverse)) { faces.delete(reverse); return; }
	faces.set(faceKey(x, y, z, normal), { x: x, y: y, z: z, J: J, n: normal, c: content });
}

/** light cache seeded with the leaves of the chunk : 1 = air, 0 = solid */
function initLightCache(chunk) {
	var cache = new Map();
	var leaves = chunk.collectGreatChildren([], chunk.J);
	for (var i = 0; i < leaves.length; i++) {
		var n = leaves[i];
		cache.set(blockKey(n.x, n.y, n.z), (n.state === DEAD_AIR) ? 1 : 0);
	}
	return cache;
}

function isAir(cache, builder, x, y, z, J) {
	var key = blockKey(x, y, z);
	var v = cache.get(key);
	if (v === undefined) {
		v = (builder.isIntersectingSurface(x, y, z, J) || builder.isGround(x, y, z, J)) ? 0 : 1;
		cache.set(key, v);
	}
	return v;
}

var NEIGH = new Int32Array(12);

/**
 * Build the vertex data of a chunk.
 * Vertex layout : x, y, z, u, v, light  (6 floats)
 */
function buildChunkMesh(root, chunk, builder) {
	var faces = collectFaces(root, chunk);
	var n = faces.length;
	if (n === 0) { return null; }
	var cache = initLightCache(chunk);
	var vertices = new Float32Array(n * 4 * 6);
	var indices = new Uint32Array(n * 6);
	var vi = 0, ii = 0, vertexBase = 0;
	for (var f = 0; f < n; f++) {
		var face = faces[f];
		var J = face.J, normal = face.n;
		var ttj = POW2[J];
		var x0 = face.x * ttj, y0 = face.y * ttj, z0 = face.z * ttj;
		var uv = texCoord(face.c, normal);
		var corners = FACE_CORNERS[normal];
		var uvIdx = FACE_UV[normal];
		// base light : darker for the faces looking away from the sun, and
		// darker for the coarse (far away) blocks
		var lightFace = (normal < 0 ? 0.6 : 1.0) * Math.max(0.35, 1 - J / 6);
		for (var c = 0; c < 4; c++) {
			var corner = corners[c];
			inFrontOfVertice(face.x, face.y, face.z, J, normal, corner, NEIGH);
			var air = 0;
			for (var k = 0; k < 4; k++) {
				air += isAir(cache, builder, NEIGH[3 * k], NEIGH[3 * k + 1], NEIGH[3 * k + 2], J);
			}
			var light = lightFace * (air / 5 + 1 / 5);
			vertices[vi++] = x0 + corner[0] * ttj;
			vertices[vi++] = y0 + corner[1] * ttj;
			vertices[vi++] = z0 + corner[2] * ttj;
			vertices[vi++] = uvIdx[c][0] ? uv[1] : uv[0];
			vertices[vi++] = uvIdx[c][1] ? uv[3] : uv[2];
			vertices[vi++] = light;
		}
		indices[ii++] = vertexBase; indices[ii++] = vertexBase + 1; indices[ii++] = vertexBase + 2;
		indices[ii++] = vertexBase; indices[ii++] = vertexBase + 2; indices[ii++] = vertexBase + 3;
		vertexBase += 4;
	}
	return { vertices: vertices, indices: indices, faceCount: n };
}

if (typeof module !== 'undefined') {
	module.exports = { buildChunkMesh: buildChunkMesh, texCoord: texCoord, metaTexCoord: metaTexCoord, collectFaces: collectFaces };
}

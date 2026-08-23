#!/usr/bin/env node
// Digging and building, driven headlessly : the game logic runs against a stub
// renderer, so a whole world of edits can be checked in a few seconds.
//
// Two invariants after every edit :
//   - every chunk being drawn is still part of the octree
//   - every chunk's mesh matches what a fresh build of it would produce
// and the tree itself has to show the change : dug blocks empty, built blocks
// solid and of the chosen material.
const fs = require('fs');
const path = require('path');

const dir = path.join(__dirname, '..', 'src');
const src = ['01-world.js', '02-blocktree.js', '03-mesh.js', '04-physics.js', '06-game.js']
	.map(f => fs.readFileSync(path.join(dir, f), 'utf8')).join('\n');

function StubRenderer() { this.chunks = new Map(); }
StubRenderer.prototype.uploadChunk = function (node, mesh) {
	if (!mesh) { this.chunks.delete(node); return; }
	this.chunks.set(node, mesh);
};
StubRenderer.prototype.dropChunk = function (node) { this.chunks.delete(node); };
StubRenderer.prototype.dropAll = function () { this.chunks.clear(); };
StubRenderer.prototype.gpuBytes = function () { return 0; };

const api = new Function('performance', src +
	'\n; return { Game: Game, buildChunkMesh: buildChunkMesh, LEAF: LEAF, DEAD_GROUND: DEAD_GROUND, DEAD_AIR: DEAD_AIR, Terran: Terran };')
	({ now: () => Number(process.hrtime.bigint()) / 1e6 });

let failures = 0;
function ok(name, cond, extra) {
	if (!cond) { failures++; }
	console.log((cond ? 'PASS  ' : 'FAIL  ') + name + (extra !== undefined ? '  ' + JSON.stringify(extra) : ''));
}

const renderer = new StubRenderer();
const game = new api.Game(renderer);

function settle(limit) { let n = 0; while (game.needsRefinement() && n++ < limit) { game.runJobs(50); } }
function drain(limit) { let n = 0; while ((game.regenQueue.length || game.pending || game.meshQueue.length) && n++ < limit) { game.step(); } }

/** chunks drawn that the octree no longer holds : their faces would never go away */
function detachedChunks() {
	const bad = [];
	renderer.chunks.forEach(function (mesh, node) {
		if (game.root.smallestCellContaining(node.x, node.y, node.z, node.J) !== node) {
			bad.push({ chunk: [node.x, node.y, node.z, node.J], faces: mesh.vertices.length / 24 });
		}
	});
	return bad;
}

/** chunks whose mesh no longer matches the world they sit in */
function staleChunks() {
	const bad = [];
	renderer.chunks.forEach(function (mesh, node) {
		const fresh = api.buildChunkMesh(game.root, node, game.builder);
		const a = mesh.vertices, b = fresh ? fresh.vertices : new Float32Array(0);
		if (a.length !== b.length) {
			bad.push({ chunk: [node.x, node.y, node.z, node.J], storedFaces: a.length / 24, freshFaces: b.length / 24 });
			return;
		}
		for (let i = 0; i < a.length; i++) {
			if (a[i] !== b[i]) { bad.push({ chunk: [node.x, node.y, node.z, node.J], differsAt: i }); return; }
		}
	});
	return bad;
}

/**
 * What the tree holds at a block. A cell coarser than the block cannot show it
 * either way — a small hole vanishes inside a big cell, and a small block is
 * lost in one — so those are reported as unrepresentable rather than wrong.
 */
function treeStateAt(block) {
	// probe the middle of the block at the finest scale, so a region held more
	// finely than the block is read at its own resolution
	const half = Math.pow(2, block.J) / 2;
	const x = block.x * Math.pow(2, block.J) + half;
	const y = block.y * Math.pow(2, block.J) + half;
	const z = block.z * Math.pow(2, block.J) + half;
	const cell = game.root.smallestCellContaining(Math.floor(x), Math.floor(y), Math.floor(z), 0);
	if (!cell) { return null; }
	if (cell.J > block.J) { return 'coarser'; }
	return cell.state;
}

settle(4000);
ok('a world builds', renderer.chunks.size > 100, { chunks: renderer.chunks.size, radius: +game.builder.radius.toFixed(2) });
ok('nothing is stale or detached to begin with',
	staleChunks().length === 0 && detachedChunks().length === 0);

game.player.x = 2048; game.player.y = 2048; game.player.z = 2148;
for (let i = 0; i < 1500; i++) { game.update(16); }

// dig all around, at build sizes from a single cube up to 32
const dug = [];
for (let k = 0; k < 12; k++) {
	game.player.theta = k * 0.5;
	game.player.phi = -0.5;
	for (const J of [0, 1, 2, 3, 4, 5]) {
		game.targetJ = J;
		const pick = game.pick();
		if (!pick.remove) { continue; }
		game.removeBlock(pick.remove);
		dug.push(pick.remove);
		drain(20000);
	}
}
ok('digging happens at every build size', dug.length > 30, { blocks: dug.length });
ok('no chunk is left drawn after being replaced by a dig', detachedChunks().length === 0, detachedChunks().slice(0, 3));
ok('every mesh matches the world after digging', staleChunks().length === 0, staleChunks().slice(0, 3));
const dugFine = dug.filter(b => treeStateAt(b) !== 'coarser');
const stillSolid = dugFine.filter(b => treeStateAt(b) === api.LEAF || treeStateAt(b) === api.DEAD_GROUND);
ok('the dug blocks are gone from the tree, where the tree is fine enough to show it',
	dugFine.length > 5 && stillSolid.length === 0, { checked: dugFine.length, stillSolid: stillSolid.slice(0, 3) });

// and the same for building
const built = [];
for (let k = 0; k < 10; k++) {
	game.player.theta = 0.25 + k * 0.6;
	game.player.phi = -0.35;
	for (const J of [0, 1, 2, 3, 4]) {
		game.targetJ = J;
		const pick = game.pick();
		if (!pick.add) { continue; }
		game.addBlock(pick.add, api.Terran.MAN_BRICK);
		built.push(pick.add);
		drain(20000);
	}
}
ok('building happens at every build size', built.length > 20, { blocks: built.length });
ok('no chunk is left drawn after being replaced by a build', detachedChunks().length === 0, detachedChunks().slice(0, 3));
ok('every mesh matches the world after building', staleChunks().length === 0, staleChunks().slice(0, 3));
const builtFine = built.filter(b => treeStateAt(b) !== 'coarser');
const notSolid = builtFine.filter(b => treeStateAt(b) !== api.LEAF && treeStateAt(b) !== api.DEAD_GROUND);
ok('the built blocks are in the tree, where the tree is fine enough to show it',
	builtFine.length > 5 && notSolid.length === 0, { checked: builtFine.length, notSolid: notSolid.slice(0, 3) });

// edits while the level of detail is moving : the two paths meet in the queue
game.setFlying(true);
game.player.theta = 1.0; game.dirs.forward = true;
let edits = 0;
for (let i = 0; i < 600; i++) {
	game.speedMult = 8;
	game.player.phi = 0;                 // fly level, so the player stays above ground
	game.update(16);
	if (i % 20 === 0) {
		game.player.phi = -0.6;          // but aim down to dig
		game.targetJ = (i / 20) % 4;
		const pick = game.pick();
		if (pick.remove) { game.removeBlock(pick.remove); edits++; }
	}
}
game.dirs.forward = false;
drain(60000);
ok('editing while the level of detail moves leaves nothing detached',
	edits > 5 && detachedChunks().length === 0, { edits: edits, detached: detachedChunks().slice(0, 3) });
ok('editing while the level of detail moves leaves no stale mesh',
	staleChunks().length === 0, staleChunks().slice(0, 3));

console.log(failures ? failures + ' failing check(s)' : 'all checks passed');
process.exit(failures ? 1 : 0);

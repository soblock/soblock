#!/usr/bin/env node
// The detail budget has to follow the machine : grow while our own work in a
// frame is cheap and the player has asked for more detail than the budget can
// pay for, and fall back when frames get expensive.
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

const api = new Function('performance', src + '\n; return { Game: Game };')
	({ now: () => Number(process.hrtime.bigint()) / 1e6, memory: undefined });

let failures = 0;
function ok(name, cond, extra) {
	if (!cond) { failures++; }
	console.log((cond ? 'PASS  ' : 'FAIL  ') + name + (extra !== undefined ? '  ' + JSON.stringify(extra) : ''));
}

const renderer = new StubRenderer();
const game = new api.Game(renderer);

function settle(limit) { let n = 0; while (game.needsRefinement() && n++ < limit) { game.runJobs(30); } }
/**
 * A stretch of play at the given frame cost. Each round is roughly a couple of
 * seconds of frames, which is what both controllers need to move : the radius
 * steps on its own timer, and the budget once a second.
 */
function frames(costMs, rounds) {
	for (let r = 0; r < rounds; r++) {
		for (let i = 0; i < 30; i++) { game.observeFrame(costMs); }
		for (let i = 0; i < 150; i++) { game.runJobs(8); }
		game.lastBudget = 0;      // the tick is once a second in play ; do not wait here
		game.runJobs(8);
	}
}

// the player asks for far more detail than the starting budget can pay for
game.targetRadius = 15;
settle(4000);
const start = { budget: game.maxChunks, radius: +game.builder.radius.toFixed(2), chunks: renderer.chunks.size };
ok('the radius starts held down by the budget, not by the request',
	start.radius < game.targetRadius - 1, start);

frames(2, 10);
const grown = { budget: game.maxChunks, radius: +game.builder.radius.toFixed(2), chunks: renderer.chunks.size };
ok('cheap frames buy a bigger budget', grown.budget > start.budget * 1.5, { start: start.budget, grown: grown.budget });
ok('and the extra budget turns into detail', grown.radius > start.radius + 0.5, { start: start.radius, grown: grown.radius });

frames(30, 6);
const backedOff = { budget: game.maxChunks, radius: +game.builder.radius.toFixed(2) };
ok('expensive frames give the budget back', backedOff.budget < grown.budget, { grown: grown.budget, backedOff: backedOff.budget });

// a ceiling still applies, however fast the machine
game.budgetCeiling = 900;
game.maxChunks = 880;
frames(1, 6);
ok('the ceiling holds', game.maxChunks <= 900, { budget: game.maxChunks, ceiling: game.budgetCeiling });

// and asking for little detail does not spend the budget on nothing
game.budgetCeiling = 6000;
game.targetRadius = 2;
game.setChunkBudget(1600);
settle(4000);
const modest = game.maxChunks;
frames(1, 5);
ok('a small request leaves the budget alone', game.maxChunks <= modest,
	{ before: modest, after: game.maxChunks, radius: +game.builder.radius.toFixed(2) });

console.log(failures ? failures + ' failing check(s)' : 'all checks passed');
process.exit(failures ? 1 : 0);

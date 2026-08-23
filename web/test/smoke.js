#!/usr/bin/env node
// Drives the built page in headless chromium and checks that the game works :
// generation, walking, flying, digging, building, saving and loading.
// Usage : node web/test/smoke.js [path-to-index.html]
const path = require('path');
const fs = require('fs');

let chromium;
try {
	chromium = require('playwright').chromium;
} catch (e) {
	try { chromium = require('/opt/node22/lib/node_modules/playwright').chromium; }
	catch (e2) { console.error('playwright is needed : npm i -D playwright'); process.exit(2); }
}

const page_path = process.argv[2] || path.join(__dirname, '..', 'index.html');
let failures = 0;
function ok(name, cond, extra) {
	if (!cond) { failures++; }
	console.log((cond ? 'PASS  ' : 'FAIL  ') + name + (extra !== undefined ? '  ' + JSON.stringify(extra) : ''));
}

(async () => {
	const browser = await chromium.launch({
		args: ['--enable-unsafe-swiftshader', '--use-gl=angle', '--use-angle=swiftshader']
	});
	const page = await browser.newPage({ viewport: { width: 800, height: 500 } });
	const errors = [];
	page.on('pageerror', e => errors.push(e.message));
	page.on('console', m => { if (m.type() === 'error') { errors.push('console: ' + m.text()); } });
	await page.goto('file://' + page_path);
	await page.waitForTimeout(400);
	ok('the page boots', await page.evaluate(() => !!window.soblock && window.soblock.state === 'menu'));

	await page.click('#btn-new');
	for (let i = 0; i < 80; i++) {
		await page.waitForTimeout(250);
		if (await page.evaluate(() => window.soblock.state) !== 'loading') { break; }
	}
	ok('a new world loads', await page.evaluate(() => window.soblock.state) === 'playing');

	const fall = await page.evaluate(() => {
		const g = window.soblock.game;
		const start = g.player.z;
		for (let i = 0; i < 1000; i++) { g.update(16); }
		const below = g.root.smallestCellContaining(
			Math.floor(g.player.x), Math.floor(g.player.y), Math.floor(g.player.z - 2.2), 0);
		return { start: start, end: g.player.z, vz: g.walk.velocity[2], ground: below && below.state };
	});
	ok('the player falls and lands on solid ground',
		fall.end < fall.start - 10 && Math.abs(fall.vz) < 0.005 && (fall.ground === 5 || fall.ground === 3), fall);

	const walked = await page.evaluate(() => {
		const g = window.soblock.game;
		g.player.theta = 0; g.player.phi = -0.3;
		const x0 = g.player.x, y0 = g.player.y;
		g.dirs.forward = true;
		for (let i = 0; i < 120; i++) { g.update(16); }
		g.dirs.forward = false;
		return Math.hypot(g.player.x - x0, g.player.y - y0);
	});
	ok('walking moves the player', walked > 1, { distance: +walked.toFixed(2) });

	const build = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		g.setFlying(true);
		g.player.z += 10; g.player.theta = 0.3; g.player.phi = -0.45;
		ui.selectMaterial(0);
		const pick = g.pick();
		if (!pick.add) { return { noTarget: true }; }
		ui.place();
		for (let i = 0; i < 60; i++) { g.runJobs(50); }
		const b = g.atoms[g.atoms.length - 1];
		g.builder.minMaxAtVertices(b[0], b[1], b[2], b[3]);
		return {
			atom: b, edits: g.atoms.length,
			state: g.builder.classify(b[0], b[1], b[2], b[3], g.builder._vMin, g.builder._vMax),
			content: (g.root.smallestCellContaining(b[0], b[1], b[2], b[3]) || {}).content
		};
	});
	ok('a placed block becomes solid brick', build.state === 5 && build.content === 10, build);

	const dig = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		g.player.theta = 0.9; g.player.phi = -0.45;
		const pick = g.pick();
		if (!pick.remove) { return { noTarget: true }; }
		ui.dig();
		for (let i = 0; i < 60; i++) { g.runJobs(50); }
		const b = pick.remove;
		g.builder.minMaxAtVertices(b.x, b.y, b.z, b.J);
		return { target: b, state: g.builder.classify(b.x, b.y, b.z, b.J, g.builder._vMin, g.builder._vMax) };
	});
	ok('a dug block becomes air', dig.state === 4, dig);

	const roam = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		g.setFlying(true);
		g.player.theta = 0.7; g.player.phi = 0.05;
		g.dirs.forward = true;
		const times = [];
		for (let i = 0; i < 400; i++) {
			const t = performance.now();
			g.speedMult = 16;
			g.update(16);
			times.push(performance.now() - t);
		}
		g.dirs.forward = false;
		times.sort((a, b) => a - b);
		return {
			travelled: +Math.hypot(g.player.x - 2048, g.player.y - 2048).toFixed(0),
			chunks: ui.renderer.chunks.size, merges: g.stats.merges,
			p95: +times[380].toFixed(1), max: +times[399].toFixed(1)
		};
	});
	ok('flying keeps the level of detail bounded', roam.chunks <= 1200 && roam.merges > 0, roam);
	ok('a frame of background work stays short', roam.p95 < 25, { p95: roam.p95, max: roam.max });

	// A chunk hides the faces it shares with a solid neighbour. When the
	// neighbour changes level of detail those faces have to be worked out
	// again, otherwise the seam keeps a stale mesh and the player sees through
	// the ground. Compare the frame with backface culling against the same
	// frame without : from outside the terrain they must agree, except along a
	// few pixels wide edges.
	await page.evaluate(() => {
		window.__countHoles = function () {
			const ui = window.soblock, gl = ui.renderer.gl;
			const w = gl.drawingBufferWidth, h = gl.drawingBufferHeight;
			function frame() {
				ui.frame(performance.now());
				const px = new Uint8Array(4 * w * h);
				gl.readPixels(0, 0, w, h, gl.RGBA, gl.UNSIGNED_BYTE, px);
				return px;
			}
			const culled = frame();
			gl.disable(gl.CULL_FACE);
			const notCulled = frame();
			gl.enable(gl.CULL_FACE);
			let holes = 0;
			for (let i = 0; i < culled.length; i += 4) {
				const sky = Math.abs(culled[i] - 135) < 4 && Math.abs(culled[i + 1] - 206) < 4 && Math.abs(culled[i + 2] - 250) < 4;
				const covered = !(Math.abs(notCulled[i] - 135) < 4 && Math.abs(notCulled[i + 1] - 206) < 4 && Math.abs(notCulled[i + 2] - 250) < 4);
				if (sky && covered) { holes++; }
			}
			return holes;
		};
	});
	const seams = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		ui.state = 'paused';                     // hold the camera still
		g.player.theta = 0.4; g.player.phi = -0.08;
		// straight after moving, with chunk rebuilds still in flight : a change
		// of level of detail has to swap all at once, neighbours included
		const churning = window.__countHoles();
		let guard = 0;
		while (g.needsRefinement() && guard++ < 5000) { g.runJobs(20); }
		const settled = window.__countHoles();
		ui.state = 'playing';
		return { churning: churning, settled: settled };
	});
	ok('no holes at the level of detail seams while chunks are being rebuilt', seams.churning < 20, seams);
	ok('no holes at the level of detail seams once settled', seams.settled < 20, seams);

	const save = await page.evaluate(() => {
		window.soblock.doSave('smoke test');
		const saves = JSON.parse(localStorage.getItem('soblock.saves'));
		return { names: Object.keys(saves), edits: saves['smoke test'].atoms.length };
	});
	ok('saving writes to local storage', save.names.indexOf('smoke test') >= 0 && save.edits >= 2, save);

	await page.reload({ timeout: 60000 });
	await page.waitForTimeout(500);
	const load = await page.evaluate(() => new Promise(resolve => {
		const ui = window.soblock;
		ui.startGame(JSON.parse(localStorage.getItem('soblock.saves'))['smoke test']);
		(function wait() {
			if (ui.state !== 'playing') { return setTimeout(wait, 300); }
			const g = ui.game, b = g.atoms[0];
			g.builder.minMaxAtVertices(b[0], b[1], b[2], b[3]);
			resolve({ edits: g.atoms.length, brick: g.builder.classify(b[0], b[1], b[2], b[3], g.builder._vMin, g.builder._vMax) });
		})();
	}));
	ok('loading a saved world restores the edits', load.edits >= 2 && load.brick === 5, load);

	// mouse look without the pointer lock (embedded frames may refuse it)
	const drag = await page.evaluate(() => {
		const ui = window.soblock;
		ui.canvas.requestPointerLock = function () { return Promise.reject(new Error('denied')); };
		const theta0 = ui.game.player.theta;
		ui.canvas.dispatchEvent(new MouseEvent('mousedown', { button: 0, bubbles: true }));
		document.dispatchEvent(new MouseEvent('mousemove', { bubbles: true, movementX: 40, movementY: 10 }));
		document.dispatchEvent(new MouseEvent('mouseup', { button: 0, bubbles: true }));
		return { turned: Math.abs(ui.game.player.theta - theta0) > 0.1 };
	});
	ok('dragging turns the camera when the pointer lock is refused', drag.turned, drag);

	ok('no page error', errors.length === 0, errors.slice(0, 3));
	await browser.close();
	console.log(failures ? failures + ' failing check(s)' : 'all checks passed');
	process.exit(failures ? 1 : 0);
})();

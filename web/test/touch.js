#!/usr/bin/env node
// Drives the built page as a phone would : an emulated iPhone viewport with a
// touch screen, no mouse and no pointer lock.
// Usage : node web/test/touch.js [path-to-index.html]
const path = require('path');

let chromium, devices;
try {
	({ chromium, devices } = require('playwright'));
} catch (e) {
	try { ({ chromium, devices } = require('/opt/node22/lib/node_modules/playwright')); }
	catch (e2) { console.error('playwright is needed : npm i -D playwright'); process.exit(2); }
}

const page_path = process.argv[2] || path.join(__dirname, '..', 'index.html');
let failures = 0;
function ok(name, cond, extra) {
	if (!cond) { failures++; }
	console.log((cond ? 'PASS  ' : 'FAIL  ') + name + (extra !== undefined ? '  ' + JSON.stringify(extra) : ''));
}

/** a touch that presses, drags through the given offsets, and lifts */
async function swipe(page, from, moves, hold) {
	await page.evaluate(([from, moves, hold]) => {
		const canvas = document.getElementById('gl');
		let id = 1;
		function touch(x, y) {
			return new Touch({ identifier: id, target: canvas, clientX: x, clientY: y });
		}
		function fire(type, x, y) {
			const t = touch(x, y);
			canvas.dispatchEvent(new TouchEvent(type, {
				bubbles: true, cancelable: true, touches: type === 'touchend' ? [] : [t],
				targetTouches: type === 'touchend' ? [] : [t], changedTouches: [t]
			}));
		}
		fire('touchstart', from[0], from[1]);
		let x = from[0], y = from[1];
		for (const m of moves) { x += m[0]; y += m[1]; fire('touchmove', x, y); }
		if (!hold) { fire('touchend', x, y); }
		return null;
	}, [from, moves, !!hold]);
}

(async () => {
	const iphone = devices['iPhone 13'];
	const browser = await chromium.launch({
		args: ['--enable-unsafe-swiftshader', '--use-gl=angle', '--use-angle=swiftshader']
	});
	const context = await browser.newContext({ ...iphone, isMobile: false });
	const page = await context.newPage();
	const errors = [];
	page.on('pageerror', e => errors.push(e.message));
	page.on('console', m => { if (m.type() === 'error') { errors.push('console: ' + m.text()); } });
	await page.goto('file://' + page_path);
	await page.waitForTimeout(500);

	const boot = await page.evaluate(() => ({
		touch: document.body.classList.contains('touch'),
		hiddenBehindTheMenu: getComputedStyle(document.getElementById('touch')).display === 'none',
		hintHidden: getComputedStyle(document.getElementById('hint')).display === 'none',
		chunkBudget: window.soblock.game.maxChunks,
		pixelRatio: window.soblock.renderer.maxPixelRatio,
		viewport: [window.innerWidth, window.innerHeight]
	}));
	ok('a touch screen turns on the thumb controls', boot.touch && boot.hintHidden, boot);
	ok('the thumb controls stay out of the way of the menu', boot.hiddenBehindTheMenu, boot);
	ok('a phone gets a smaller detail budget', boot.chunkBudget === 500 && boot.pixelRatio === 1.5, boot);

	await page.tap('#btn-new');
	for (let i = 0; i < 80; i++) {
		await page.waitForTimeout(250);
		if (await page.evaluate(() => window.soblock.state) !== 'loading') { break; }
	}
	ok('a world loads from a tap', await page.evaluate(() => window.soblock.state) === 'playing');
	ok('the thumb controls come back with the game',
		await page.evaluate(() => getComputedStyle(document.getElementById('touch')).display !== 'none'));

	// the canvas must fill the screen, and the controls stay inside it
	const layout = await page.evaluate(() => {
		const r = document.getElementById('gl').getBoundingClientRect();
		const inside = id => {
			const b = document.getElementById(id).getBoundingClientRect();
			return b.width > 0 && b.right <= window.innerWidth + 1 && b.bottom <= window.innerHeight + 1 && b.left >= -1 && b.top >= -1;
		};
		return { canvas: [Math.round(r.width), Math.round(r.height)],
			stick: inside('stick'), actions: inside('touch-actions'), top: inside('touch-top'),
			vert: inside('touch-vert'), palette: inside('palette') };
	});
	ok('the canvas fills the phone screen', layout.canvas[0] === boot.viewport[0] && layout.canvas[1] === boot.viewport[1], layout.canvas);
	ok('every thumb control is on screen', layout.stick && layout.actions && layout.top && layout.vert && layout.palette, layout);

	// settle on the ground before moving
	await page.evaluate(() => { const g = window.soblock.game; for (let i = 0; i < 900; i++) { g.update(16); } });

	// the thumb stick walks the player
	const walk = await page.evaluate(() => { const g = window.soblock.game; g.player.theta = 0; return [g.player.x, g.player.y]; });
	await swipe(page, [90, 500], [[0, -50]], true);         // push the stick up : walk forward
	const stuck = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		const axes = [g.dirs.axisX, g.dirs.axisY];
		for (let i = 0; i < 120; i++) { g.update(16); }
		return { axes: axes, moved: Math.hypot(g.player.x - window.__x0, g.player.y - window.__y0),
			stickActive: document.getElementById('stick').classList.contains('active'),
			x: g.player.x, y: g.player.y };
	});
	const walked = Math.hypot(stuck.x - walk[0], stuck.y - walk[1]);
	ok('the thumb stick pushes forward', stuck.axes[1] > 0.5 && Math.abs(stuck.axes[0]) < 0.2, { axes: stuck.axes });
	ok('the stick appears under the thumb', stuck.stickActive);
	ok('the player walks with the stick', walked > 1, { distance: +walked.toFixed(2) });

	// lifting the thumb stops the player
	await swipe(page, [90, 450], [[0, 0]], false);
	const stopped = await page.evaluate(() => {
		const g = window.soblock.game;
		return { axes: [g.dirs.axisX, g.dirs.axisY], stickActive: document.getElementById('stick').classList.contains('active') };
	});
	ok('lifting the thumb stops the player', stopped.axes[0] === 0 && stopped.axes[1] === 0 && !stopped.stickActive, stopped);

	// dragging on the right half looks around
	const before = await page.evaluate(() => [window.soblock.game.player.theta, window.soblock.game.player.phi]);
	await swipe(page, [300, 300], [[-40, 0], [-40, 0], [0, 30]]);
	const after = await page.evaluate(() => [window.soblock.game.player.theta, window.soblock.game.player.phi]);
	ok('dragging the right half turns the camera',
		Math.abs(after[0] - before[0]) > 0.1 && Math.abs(after[1] - before[1]) > 0.05,
		{ dTheta: +(after[0] - before[0]).toFixed(3), dPhi: +(after[1] - before[1]).toFixed(3) });

	// the build and dig buttons
	const build = await page.evaluate(async () => {
		const ui = window.soblock, g = ui.game;
		g.setFlying(true); g.player.z += 10; g.player.theta = 0.3; g.player.phi = -0.45;
		const before = g.atoms.length;
		const b = document.getElementById('t-build');
		b.dispatchEvent(new TouchEvent('touchstart', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		b.dispatchEvent(new TouchEvent('touchend', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		for (let i = 0; i < 60; i++) { g.runJobs(50); }
		const atom = g.atoms[g.atoms.length - 1];
		let solid = null;
		if (atom) {
			g.builder.minMaxAtVertices(atom[0], atom[1], atom[2], atom[3]);
			solid = g.builder.classify(atom[0], atom[1], atom[2], atom[3], g.builder._vMin, g.builder._vMax);
		}
		return { before: before, after: g.atoms.length, solid: solid, material: atom && atom[4] };
	});
	ok('the build button places a block', build.after === build.before + 1 && build.solid === 5, build);

	const dig = await page.evaluate(async () => {
		const ui = window.soblock, g = ui.game;
		g.player.theta = 0.9;
		const target = g.pick().remove;
		const b = document.getElementById('t-dig');
		b.dispatchEvent(new TouchEvent('touchstart', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		b.dispatchEvent(new TouchEvent('touchend', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		for (let i = 0; i < 60; i++) { g.runJobs(50); }
		if (!target) { return { noTarget: true }; }
		g.builder.minMaxAtVertices(target.x, target.y, target.z, target.J);
		return { state: g.builder.classify(target.x, target.y, target.z, target.J, g.builder._vMin, g.builder._vMax) };
	});
	ok('the dig button empties a block', dig.state === 4, dig);

	// up button, build size and the mode switch
	const buttons = await page.evaluate(() => {
		const ui = window.soblock, g = ui.game;
		function press(id) {
			const n = document.getElementById(id);
			n.dispatchEvent(new TouchEvent('touchstart', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
			n.dispatchEvent(new TouchEvent('touchend', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		}
		press('t-size-up'); press('t-size-up');
		const size = { j: g.targetJ, readout: document.getElementById('t-size').textContent };
		press('t-size-down');
		const smaller = g.targetJ;
		const flyingBefore = g.flying;
		press('t-mode');
		const mode = { flying: g.flying, label: document.getElementById('t-mode').textContent };
		// hold the up button : the player should rise
		g.setFlying(true);
		const z0 = g.player.z;
		const up = document.getElementById('t-up');
		up.dispatchEvent(new TouchEvent('touchstart', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		const held = g.dirs.up;
		for (let i = 0; i < 100; i++) { g.update(16); }
		up.dispatchEvent(new TouchEvent('touchend', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		return { size, smaller, mode, flippedMode: mode.flying !== flyingBefore, held, climb: g.player.z - z0, upAfter: g.dirs.up };
	});
	ok('the size buttons change the build size', buttons.size.j === 2 && buttons.size.readout === '4' && buttons.smaller === 1, buttons.size);
	ok('the mode button switches walking and flying', buttons.flippedMode && /Walk|Fly/.test(buttons.mode.label), buttons.mode);
	ok('holding the up button climbs', buttons.held && buttons.climb > 3 && !buttons.upAfter, { climb: +buttons.climb.toFixed(1) });

	// the menu button, since a phone has no escape key
	await page.evaluate(() => {
		const n = document.getElementById('t-menu');
		n.dispatchEvent(new TouchEvent('touchstart', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
		n.dispatchEvent(new TouchEvent('touchend', { bubbles: true, cancelable: true, touches: [], changedTouches: [] }));
	});
	const paused = await page.evaluate(() => ({
		state: window.soblock.state,
		menuShown: getComputedStyle(document.getElementById('menu')).display !== 'none',
		axes: [window.soblock.game.dirs.axisX, window.soblock.game.dirs.axisY]
	}));
	ok('the menu button pauses the game', paused.state === 'paused' && paused.menuShown, paused);

	await page.tap('#btn-resume');
	ok('tapping resume goes back to the game', await page.evaluate(() => window.soblock.state) === 'playing');

	// a material can be picked by tapping the palette
	await page.evaluate(() => {
		const slot = document.querySelectorAll('#palette .slot')[4];
		slot.dispatchEvent(new MouseEvent('click', { bubbles: true }));
	});
	ok('tapping the palette picks a material', await page.evaluate(() => window.soblock.game.targetContent) === 2);

	ok('no page error', errors.length === 0, errors.slice(0, 3));
	await context.close();

	// the same page held sideways : a phone in landscape is short, and the
	// heads up display must not creep over the crosshair
	const wide = await browser.newContext({ ...iphone, viewport: { width: 844, height: 390 }, isMobile: false });
	const page2 = await wide.newPage();
	page2.on('pageerror', e => errors.push('landscape: ' + e.message));
	await page2.goto('file://' + page_path);
	await page2.waitForTimeout(400);
	await page2.tap('#btn-new');
	for (let i = 0; i < 80; i++) {
		await page2.waitForTimeout(250);
		if (await page2.evaluate(() => window.soblock.state) !== 'loading') { break; }
	}
	const sideways = await page2.evaluate(() => {
		const box = id => document.getElementById(id).getBoundingClientRect();
		const onScreen = b => b.width > 0 && b.left >= -1 && b.top >= -1 &&
			b.right <= window.innerWidth + 1 && b.bottom <= window.innerHeight + 1;
		// the crosshair and a margin around it have to stay clear
		const aim = { left: window.innerWidth / 2 - 40, right: window.innerWidth / 2 + 40,
			top: window.innerHeight / 2 - 40, bottom: window.innerHeight / 2 + 40 };
		const overlapsAim = b => !(b.right < aim.left || b.left > aim.right || b.bottom < aim.top || b.top > aim.bottom);
		const ids = ['stick', 'touch-actions', 'touch-vert', 'touch-top', 'palette', 'build-info'];
		const off = ids.filter(id => !onScreen(box(id)));
		const overAim = ids.filter(id => overlapsAim(box(id)));
		return { off: off, overAim: overAim, height: window.innerHeight };
	});
	ok('sideways : every control is on screen', sideways.off.length === 0, sideways);
	ok('sideways : nothing covers the crosshair', sideways.overAim.length === 0, sideways);
	await wide.close();

	await browser.close();
	console.log(failures ? failures + ' failing check(s)' : 'all checks passed');
	process.exit(failures ? 1 : 0);
})();

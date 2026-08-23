// -----------------------------------------------------------------------------
// SoBlock - browser front end : input, heads up display, menus, saved games
// -----------------------------------------------------------------------------

var SAVE_KEY = 'soblock.saves';
var OPTIONS_KEY = 'soblock.options';
var LOADING_MS = 3500;   // after that the world keeps refining while playing

var KEY_LABELS = [
	['W A S D', 'Move'],
	['Space', 'Jump / fly up'],
	['C', 'Fly down'],
	['Shift (or R T Y U)', 'Speed x4 / x4 x16 x64 x256'],
	['F', 'Toggle walk and free flight'],
	['Left click / Enter', 'Place a block'],
	['Right click / Backspace', 'Dig a block'],
	['Z / X or mouse wheel', 'Bigger / smaller build size'],
	['1 . . 6', 'Pick a material'],
	['L / \\', 'More / less level of detail'],
	['O', 'Show the octree chunks'],
	['H', 'Show the debug stats'],
	['Esc', 'Menu']
];

function el(id) { return document.getElementById(id); }

function loadOptions() {
	var o = { sensitivity: 0.01, radius: 6, invertY: false };
	try {
		var raw = localStorage.getItem(OPTIONS_KEY);
		if (raw) {
			var parsed = JSON.parse(raw);
			for (var k in parsed) { if (k in o) { o[k] = parsed[k]; } }
		}
	} catch (e) { /* private mode, defaults are fine */ }
	return o;
}

function saveOptions(o) {
	try { localStorage.setItem(OPTIONS_KEY, JSON.stringify(o)); } catch (e) { }
}

function loadSaves() {
	try {
		var raw = localStorage.getItem(SAVE_KEY);
		return raw ? JSON.parse(raw) : {};
	} catch (e) { return {}; }
}

function writeSaves(saves) {
	try {
		localStorage.setItem(SAVE_KEY, JSON.stringify(saves));
		return true;
	} catch (e) {
		return false;
	}
}

function UI() {
	this.canvas = el('gl');
	this.renderer = new Renderer(this.canvas);
	this.game = new Game(this.renderer);
	this.options = loadOptions();
	this.game.builder.radius = this.options.radius;
	this.state = 'menu';      // menu | loading | playing | paused
	this.showStats = false;
	this.showOutlines = false;
	this.lastTime = performance.now();
	this.fpsTime = this.lastTime;
	this.fpsFrames = 0;
	this.fps = 0;
	this.bindInput();
	this.buildPalette();
	this.buildControlsList();
	this.showMenu('main');
}

// --- texture ------------------------------------------------------------------

UI.prototype.loadTexture = function (cb) {
	var self = this;
	var img = new Image();
	img.onload = function () {
		self.renderer.setTexture(img);
		cb();
	};
	img.onerror = function () { cb(new Error('the terrain texture could not be decoded')); };
	img.src = TERRAIN_TEXTURE_DATA_URI;
};

// --- heads up display ---------------------------------------------------------

UI.prototype.buildPalette = function () {
	if (!document.getElementById('atlas-style')) {
		var style = document.createElement('style');
		style.id = 'atlas-style';
		style.textContent = '.tile { background-image: url(' + TERRAIN_TEXTURE_DATA_URI + '); }';
		document.head.appendChild(style);
	}
	var bar = el('palette');
	bar.innerHTML = '';
	this.paletteButtons = [];
	var self = this;
	for (var i = 0; i < SELECTABLE.length; i++) {
		(function (index) {
			var terran = SELECTABLE[index];
			var slot = document.createElement('div');
			slot.className = 'slot';
			var tile = document.createElement('div');
			tile.className = 'tile';
			var mt = metaTexCoord(terran, 0);
			tile.style.backgroundPosition = (mt[0] * 100 / 15) + '% ' + (mt[1] * 100 / 15) + '%';
			var num = document.createElement('span');
			num.className = 'num';
			num.textContent = String(index + 1);
			slot.appendChild(tile);
			slot.appendChild(num);
			slot.title = TERRAN_LABELS[terran];
			slot.addEventListener('click', function () { self.selectMaterial(index); });
			bar.appendChild(slot);
			self.paletteButtons.push(slot);
		})(i);
	}
	this.selectMaterial(0);
};

UI.prototype.selectMaterial = function (index) {
	this.game.targetContent = SELECTABLE[index];
	for (var i = 0; i < this.paletteButtons.length; i++) {
		this.paletteButtons[i].classList.toggle('selected', i === index);
	}
	el('material-name').textContent = TERRAN_LABELS[SELECTABLE[index]];
};

UI.prototype.buildControlsList = function () {
	var html = KEY_LABELS.map(function (k) {
		return '<div class="key">' + k[0] + '</div><div class="what">' + k[1] + '</div>';
	}).join('');
	el('controls-list').innerHTML = html;
	el('controls-list-menu').innerHTML = html;
};

UI.prototype.updateHud = function () {
	var g = this.game;
	el('build-size').textContent = POW2[g.targetJ] + (g.targetJ ? ' × ' + POW2[g.targetJ] + ' × ' + POW2[g.targetJ] : '');
	el('mode-name').textContent = g.flying ? 'free flight' : 'walking';
	if (this.showStats) {
		var r = this.renderer;
		el('stats').innerHTML =
			'fps ' + this.fps.toFixed(0) +
			'<br>x ' + g.player.x.toFixed(1) + '  y ' + g.player.y.toFixed(1) + '  z ' + g.player.z.toFixed(1) +
			'<br>chunks ' + r.chunks.size + ' (' + r.drawCalls + ' drawn)' +
			'<br>triangles ' + (r.trianglesDrawn / 1000).toFixed(1) + 'k' +
			'<br>gpu ' + (r.gpuBytes() / 1e6).toFixed(1) + ' MB' +
			'<br>lod radius ' + g.builder.radius +
			'<br>splits ' + g.stats.splits + '  merges ' + g.stats.merges +
			'<br>work ' + g.stats.lodMs.toFixed(1) + ' ms/frame' +
			'<br>edits ' + g.atoms.length;
	}
};

// --- menus --------------------------------------------------------------------

UI.prototype.showMenu = function (which) {
	el('menu').style.display = which ? 'flex' : 'none';
	var panels = ['main', 'pause', 'options', 'load', 'save', 'controls'];
	for (var i = 0; i < panels.length; i++) {
		el('panel-' + panels[i]).style.display = (panels[i] === which) ? 'block' : 'none';
	}
	if (which === 'load') { this.refreshSaveList('load'); }
	if (which === 'save') { this.refreshSaveList('save'); }
	if (which === 'options') { this.refreshOptions(); }
	el('hud').style.display = which ? 'none' : 'block';
};

UI.prototype.refreshOptions = function () {
	el('opt-sensitivity').value = String(this.options.sensitivity * 1000);
	el('opt-sensitivity-value').textContent = (this.options.sensitivity * 1000).toFixed(0);
	el('opt-radius').value = String(this.options.radius);
	el('opt-radius-value').textContent = String(this.options.radius);
	el('opt-invert').checked = !!this.options.invertY;
};

UI.prototype.refreshSaveList = function (mode) {
	var saves = loadSaves();
	var names = Object.keys(saves).sort(function (a, b) { return saves[b].date - saves[a].date; });
	var list = el(mode === 'load' ? 'load-list' : 'save-list');
	list.innerHTML = '';
	var self = this;
	if (!names.length) {
		list.innerHTML = '<p class="empty">No saved game yet.</p>';
	}
	names.forEach(function (name) {
		var row = document.createElement('div');
		row.className = 'save-row';
		var when = new Date(saves[name].date);
		var info = document.createElement('div');
		info.innerHTML = '<b>' + escapeHtml(name) + '</b><br><small>' + when.toLocaleString() +
			' — ' + (saves[name].atoms ? saves[name].atoms.length : 0) + ' edits</small>';
		row.appendChild(info);
		var buttons = document.createElement('div');
		var action = document.createElement('button');
		action.textContent = (mode === 'load') ? 'Load' : 'Overwrite';
		action.addEventListener('click', function () {
			if (mode === 'load') { self.startGame(saves[name]); }
			else { self.doSave(name); }
		});
		buttons.appendChild(action);
		var del = document.createElement('button');
		del.className = 'danger';
		del.textContent = 'Delete';
		del.addEventListener('click', function () {
			var s = loadSaves();
			delete s[name];
			writeSaves(s);
			self.refreshSaveList(mode);
		});
		buttons.appendChild(del);
		row.appendChild(buttons);
		list.appendChild(row);
	});
};

function escapeHtml(s) {
	return String(s).replace(/[&<>"']/g, function (c) {
		return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
	});
}

UI.prototype.doSave = function (name) {
	name = (name || '').trim();
	if (!name) { return; }
	var saves = loadSaves();
	saves[name] = this.game.serialize(name);
	if (writeSaves(saves)) {
		el('save-feedback').textContent = 'Saved "' + name + '".';
	} else {
		el('save-feedback').textContent = 'Could not save : the browser storage is full.';
	}
	this.refreshSaveList('save');
};

// --- game flow ----------------------------------------------------------------

UI.prototype.startGame = function (save) {
	var self = this;
	this.state = 'loading';
	this.showMenu(null);
	el('loading').style.display = 'flex';
	el('loading-bar').style.width = '0%';
	// let the browser paint the loading screen before the world is built
	requestAnimationFrame(function () {
		if (save) { self.game.deserialize(save); } else { self.game.newWorld(); }
		self.loadStart = performance.now();
	});
};

UI.prototype.stepLoading = function () {
	if (this.loadStart === undefined) { return; }
	this.game.runJobs(24);
	var elapsed = performance.now() - this.loadStart;
	// the world keeps getting sharper while playing : we only wait for the
	// neighbourhood of the player, or for a few seconds, whichever comes first
	var done = !this.game.needsRefinement() || elapsed > LOADING_MS;
	var progress = done ? 1 : Math.max(elapsed / LOADING_MS, this.renderer.chunks.size / this.game.maxChunks);
	el('loading-bar').style.width = (Math.min(0.99, progress) * 100).toFixed(0) + '%';
	if (done) {
		el('loading').style.display = 'none';
		this.loadStart = undefined;
		this.state = 'playing';
		this.lastTime = performance.now();
		this.requestPointerLock();
	}
};

UI.prototype.requestPointerLock = function () {
	var c = this.canvas;
	if (c.requestPointerLock) {
		var p = c.requestPointerLock();
		if (p && p.catch) { p.catch(function () { }); }
	}
};

UI.prototype.pause = function () {
	if (this.state !== 'playing') { return; }
	this.state = 'paused';
	this.clearKeys();
	if (document.pointerLockElement) { document.exitPointerLock(); }
	this.showMenu('pause');
};

UI.prototype.resume = function () {
	if (this.state !== 'paused') { return; }
	this.state = 'playing';
	this.showMenu(null);
	this.lastTime = performance.now();
	this.requestPointerLock();
};

UI.prototype.clearKeys = function () {
	var d = this.game.dirs;
	d.forward = d.backward = d.left = d.right = d.up = d.down = false;
	this.game.speedMult = 1;
	this.speedKeys = {};
	this.drag = null;
};

// --- input --------------------------------------------------------------------

UI.prototype.bindInput = function () {
	var self = this;
	this.speedKeys = {};

	document.addEventListener('keydown', function (e) {
		if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA')) { return; }
		if (self.onKey(e.code, true, e)) { e.preventDefault(); }
	});
	document.addEventListener('keyup', function (e) {
		if (self.onKey(e.code, false, e)) { e.preventDefault(); }
	});

	// Mouse look uses the pointer lock when the browser grants it, and falls
	// back to dragging with a button held down when it does not (some embedded
	// frames refuse the lock).
	this.canvas.addEventListener('mousedown', function (e) {
		if (self.state !== 'playing') { return; }
		if (document.pointerLockElement) {
			if (e.button === 0) { self.place(); }
			if (e.button === 2) { self.dig(); }
			return;
		}
		self.drag = { button: e.button, moved: 0, time: performance.now() };
		self.requestPointerLock();
		e.preventDefault();
	});
	document.addEventListener('mouseup', function (e) {
		if (self.state !== 'playing' || !self.drag || document.pointerLockElement) { self.drag = null; return; }
		var quick = performance.now() - self.drag.time < 350;
		if (self.drag.moved < 6 && quick) {
			if (self.drag.button === 0) { self.place(); }
			if (self.drag.button === 2) { self.dig(); }
		}
		self.drag = null;
	});
	this.canvas.addEventListener('contextmenu', function (e) { e.preventDefault(); });

	document.addEventListener('mousemove', function (e) {
		if (self.state !== 'playing') { return; }
		var locked = !!document.pointerLockElement;
		if (!locked && !self.drag) { return; }
		var dx = e.movementX || 0, dy = e.movementY || 0;
		if (self.drag) { self.drag.moved += Math.abs(dx) + Math.abs(dy); }
		var s = self.options.sensitivity;
		var p = self.game.player;
		p.theta -= dx * s;
		p.phi += (self.options.invertY ? 1 : -1) * dy * s;
		var lim = Math.PI / 2 - 0.01;
		if (p.phi > lim) { p.phi = lim; }
		if (p.phi < -lim) { p.phi = -lim; }
	});

	this.canvas.addEventListener('wheel', function (e) {
		if (self.state !== 'playing') { return; }
		self.game.changeTargetJ(e.deltaY < 0 ? 1 : -1);
		e.preventDefault();
	}, { passive: false });

	document.addEventListener('pointerlockchange', function () {
		if (!document.pointerLockElement && self.state === 'playing') { self.pause(); }
	});

	window.addEventListener('blur', function () { self.clearKeys(); });
};

UI.prototype.onKey = function (code, down, event) {
	var g = this.game;
	if (code === 'Escape') {
		if (down) {
			if (this.state === 'playing') { this.pause(); }
			else if (this.state === 'paused') { this.resume(); }
		}
		return false; // let the browser release the pointer lock
	}
	if (this.state !== 'playing') { return false; }
	var d = g.dirs;
	switch (code) {
		case 'KeyW': case 'ArrowUp': d.forward = down; return true;
		case 'KeyS': case 'ArrowDown': d.backward = down; return true;
		case 'KeyA': case 'ArrowLeft': d.left = down; return true;
		case 'KeyD': case 'ArrowRight': d.right = down; return true;
		case 'Space': d.up = down; return true;
		case 'KeyC': d.down = down; return true;
		case 'ShiftLeft': case 'ShiftRight': this.speedKeys.shift = down; this.updateSpeed(); return true;
		case 'KeyR': this.speedKeys.r = down; this.updateSpeed(); return true;
		case 'KeyT': this.speedKeys.t = down; this.updateSpeed(); return true;
		case 'KeyY': this.speedKeys.y = down; this.updateSpeed(); return true;
		case 'KeyU': this.speedKeys.u = down; this.updateSpeed(); return true;
	}
	if (!down) { return false; }
	switch (code) {
		case 'KeyF': g.setFlying(!g.flying); return true;
		case 'Enter': this.place(); return true;
		case 'Backspace': this.dig(); return true;
		case 'KeyZ': g.changeTargetJ(1); return true;
		case 'KeyX': g.changeTargetJ(-1); return true;
		case 'KeyO': this.showOutlines = !this.showOutlines; return true;
		case 'KeyH': this.showStats = !this.showStats; el('stats').style.display = this.showStats ? 'block' : 'none'; return true;
		case 'KeyL': g.builder.radius = Math.min(512, g.builder.radius + 1); this.options.radius = g.builder.radius; saveOptions(this.options); return true;
		case 'Backslash': g.builder.radius = Math.max(1, g.builder.radius - 1); this.options.radius = g.builder.radius; saveOptions(this.options); return true;
		case 'Digit1': case 'Digit2': case 'Digit3': case 'Digit4': case 'Digit5': case 'Digit6':
			this.selectMaterial(parseInt(code.slice(5), 10) - 1);
			return true;
	}
	return false;
};

UI.prototype.updateSpeed = function () {
	var k = this.speedKeys;
	var mult = 1;
	if (k.shift) { mult = 4; }
	if (k.r) { mult = 4; }
	if (k.t) { mult = 16; }
	if (k.y) { mult = 64; }
	if (k.u) { mult = 256; }
	this.game.speedMult = mult;
};

UI.prototype.place = function () {
	var pick = this.game.pick();
	if (pick.add) { this.game.addBlock(pick.add, this.game.targetContent); }
};

UI.prototype.dig = function () {
	var pick = this.game.pick();
	if (pick.remove) { this.game.removeBlock(pick.remove); }
};

// --- main loop ----------------------------------------------------------------

UI.prototype.frame = function (now) {
	var dt = now - this.lastTime;
	this.lastTime = now;
	this.fpsFrames++;
	if (now - this.fpsTime > 500) {
		this.fps = this.fpsFrames * 1000 / (now - this.fpsTime);
		this.fpsFrames = 0;
		this.fpsTime = now;
	}

	if (this.state === 'loading') {
		this.stepLoading();
	} else if (this.state === 'playing') {
		this.game.update(dt);
	} else if (this.state === 'paused' || this.state === 'menu') {
		// keep building the world in the background, gently
		this.game.runJobs(4);
	}

	var highlights = [];
	if (this.state === 'playing') {
		var pick = this.game.pick();
		if (pick.remove) { highlights.push({ block: pick.remove, color: [1, 0, 1] }); }
		if (pick.add) { highlights.push({ block: pick.add, color: [0, 1, 1] }); }
	}
	if (this.showOutlines) {
		var self = this;
		this.renderer.chunks.forEach(function (c, node) {
			highlights.push({ block: node, color: [0.2, 0.2, 0.2] });
		});
	}
	this.renderer.render(this.game.player, highlights);
	this.updateHud();
};

UI.prototype.start = function () {
	var self = this;
	function loop(now) {
		try {
			self.frame(now);
		} catch (err) {
			showFatal(err);
			return;
		}
		requestAnimationFrame(loop);
	}
	requestAnimationFrame(loop);
};

function showFatal(err) {
	var box = el('fatal');
	box.style.display = 'block';
	box.textContent = 'SoBlock hit an error : ' + (err && err.message ? err.message : err);
	if (window.console) { console.error(err); }
}

// --- boot ---------------------------------------------------------------------

function boot() {
	var ui;
	try {
		ui = new UI();
	} catch (err) {
		showFatal(err);
		return;
	}
	window.soblock = ui;

	el('btn-new').addEventListener('click', function () { ui.startGame(null); });
	el('btn-load').addEventListener('click', function () { ui.showMenu('load'); });
	el('btn-controls').addEventListener('click', function () { ui.showMenu('controls'); });
	el('btn-options').addEventListener('click', function () { ui.showMenu('options'); });
	el('btn-resume').addEventListener('click', function () { ui.resume(); });
	el('btn-save').addEventListener('click', function () { ui.showMenu('save'); });
	el('btn-load-pause').addEventListener('click', function () { ui.showMenu('load'); });
	el('btn-options-pause').addEventListener('click', function () { ui.showMenu('options'); });
	el('btn-quit').addEventListener('click', function () {
		ui.state = 'menu';
		ui.showMenu('main');
	});
	var backButtons = document.querySelectorAll('[data-back]');
	for (var i = 0; i < backButtons.length; i++) {
		backButtons[i].addEventListener('click', function () {
			ui.showMenu(ui.state === 'menu' ? 'main' : 'pause');
		});
	}
	el('btn-do-save').addEventListener('click', function () {
		ui.doSave(el('save-name').value);
	});
	el('save-name').addEventListener('keydown', function (e) {
		if (e.key === 'Enter') { ui.doSave(el('save-name').value); }
	});
	el('opt-sensitivity').addEventListener('input', function (e) {
		ui.options.sensitivity = parseFloat(e.target.value) / 1000;
		el('opt-sensitivity-value').textContent = e.target.value;
		saveOptions(ui.options);
	});
	el('opt-radius').addEventListener('input', function (e) {
		ui.options.radius = parseInt(e.target.value, 10);
		ui.game.builder.radius = ui.options.radius;
		el('opt-radius-value').textContent = e.target.value;
		saveOptions(ui.options);
	});
	el('opt-invert').addEventListener('change', function (e) {
		ui.options.invertY = e.target.checked;
		saveOptions(ui.options);
	});

	ui.loadTexture(function (err) {
		if (err) { showFatal(err); return; }
		ui.start();
	});
}

if (typeof document !== 'undefined') {
	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', boot);
	} else {
		boot();
	}
}

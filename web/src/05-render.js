// -----------------------------------------------------------------------------
// SoBlock - WebGL2 renderer
// One vertex buffer per chunk of the octree, uploaded when the chunk is built
// and dropped when the chunk is split or merged.
// -----------------------------------------------------------------------------

var SKY = [135 / 255.01, 206 / 255, 250 / 255];

// --- small matrix helpers (column major, like OpenGL) --------------------------
function mat4Identity(m) {
	m[0] = 1; m[1] = 0; m[2] = 0; m[3] = 0;
	m[4] = 0; m[5] = 1; m[6] = 0; m[7] = 0;
	m[8] = 0; m[9] = 0; m[10] = 1; m[11] = 0;
	m[12] = 0; m[13] = 0; m[14] = 0; m[15] = 1;
	return m;
}

function mat4Perspective(m, fovyDeg, aspect, near, far) {
	var f = 1 / Math.tan(fovyDeg * Math.PI / 360);
	mat4Identity(m);
	m[0] = f / aspect; m[5] = f;
	m[10] = (far + near) / (near - far); m[11] = -1;
	m[14] = 2 * far * near / (near - far); m[15] = 0;
	return m;
}

function mat4LookAt(m, ex, ey, ez, cx, cy, cz, ux, uy, uz) {
	var fx = cx - ex, fy = cy - ey, fz = cz - ez;
	var rl = 1 / Math.hypot(fx, fy, fz);
	fx *= rl; fy *= rl; fz *= rl;
	var sx = fy * uz - fz * uy, sy = fz * ux - fx * uz, sz = fx * uy - fy * ux;
	rl = 1 / Math.hypot(sx, sy, sz);
	sx *= rl; sy *= rl; sz *= rl;
	var tx = sy * fz - sz * fy, ty = sz * fx - sx * fz, tz = sx * fy - sy * fx;
	m[0] = sx; m[1] = tx; m[2] = -fx; m[3] = 0;
	m[4] = sy; m[5] = ty; m[6] = -fy; m[7] = 0;
	m[8] = sz; m[9] = tz; m[10] = -fz; m[11] = 0;
	m[12] = -(sx * ex + sy * ey + sz * ez);
	m[13] = -(tx * ex + ty * ey + tz * ez);
	m[14] = fx * ex + fy * ey + fz * ez;
	m[15] = 1;
	return m;
}

function mat4Mul(out, a, b) {
	for (var i = 0; i < 4; i++) {
		var b0 = b[i * 4], b1 = b[i * 4 + 1], b2 = b[i * 4 + 2], b3 = b[i * 4 + 3];
		out[i * 4] = a[0] * b0 + a[4] * b1 + a[8] * b2 + a[12] * b3;
		out[i * 4 + 1] = a[1] * b0 + a[5] * b1 + a[9] * b2 + a[13] * b3;
		out[i * 4 + 2] = a[2] * b0 + a[6] * b1 + a[10] * b2 + a[14] * b3;
		out[i * 4 + 3] = a[3] * b0 + a[7] * b1 + a[11] * b2 + a[15] * b3;
	}
	return out;
}

/** the six frustum planes of a view projection matrix, as [a,b,c,d] rows */
function frustumPlanes(m, out) {
	for (var i = 0; i < 3; i++) {
		var s = i * 2;
		out[s * 4 + 0] = m[3] + m[i];
		out[s * 4 + 1] = m[7] + m[4 + i];
		out[s * 4 + 2] = m[11] + m[8 + i];
		out[s * 4 + 3] = m[15] + m[12 + i];
		out[(s + 1) * 4 + 0] = m[3] - m[i];
		out[(s + 1) * 4 + 1] = m[7] - m[4 + i];
		out[(s + 1) * 4 + 2] = m[11] - m[8 + i];
		out[(s + 1) * 4 + 3] = m[15] - m[12 + i];
	}
	return out;
}

function boxOutsideFrustum(planes, x0, y0, z0, x1, y1, z1) {
	for (var p = 0; p < 6; p++) {
		var a = planes[p * 4], b = planes[p * 4 + 1], c = planes[p * 4 + 2], d = planes[p * 4 + 3];
		// the positive vertex : if it is behind the plane the whole box is out
		var vx = (a >= 0) ? x1 : x0, vy = (b >= 0) ? y1 : y0, vz = (c >= 0) ? z1 : z0;
		if (a * vx + b * vy + c * vz + d < 0) { return true; }
	}
	return false;
}

// --- shaders -------------------------------------------------------------------

var TERRAIN_VS = [
	'#version 300 es',
	'in vec3 aPos;',
	'in vec2 aUV;',
	'in float aLight;',
	'uniform mat4 uMVP;',
	'out vec2 vUV;',
	'out float vLight;',
	'void main() {',
	'  vUV = aUV;',
	'  vLight = aLight;',
	'  gl_Position = uMVP * vec4(aPos, 1.0);',
	'}'
].join('\n');

var TERRAIN_FS = [
	'#version 300 es',
	'precision highp float;',
	'in vec2 vUV;',
	'in float vLight;',
	'uniform sampler2D uTex;',
	'out vec4 fragColor;',
	'void main() {',
	'  vec3 c = texture(uTex, vUV).rgb * clamp(vLight, 0.0, 1.0);',
	'  fragColor = vec4(c, 1.0);',
	'}'
].join('\n');

var LINE_VS = [
	'#version 300 es',
	'in vec3 aPos;',
	'uniform mat4 uMVP;',
	'void main() { gl_Position = uMVP * vec4(aPos, 1.0); }'
].join('\n');

var LINE_FS = [
	'#version 300 es',
	'precision highp float;',
	'uniform vec3 uColor;',
	'out vec4 fragColor;',
	'void main() { fragColor = vec4(uColor, 1.0); }'
].join('\n');

function compile(gl, type, source) {
	var sh = gl.createShader(type);
	gl.shaderSource(sh, source);
	gl.compileShader(sh);
	if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) {
		throw new Error('shader: ' + gl.getShaderInfoLog(sh));
	}
	return sh;
}

function program(gl, vs, fs) {
	var p = gl.createProgram();
	gl.attachShader(p, compile(gl, gl.VERTEX_SHADER, vs));
	gl.attachShader(p, compile(gl, gl.FRAGMENT_SHADER, fs));
	gl.linkProgram(p);
	if (!gl.getProgramParameter(p, gl.LINK_STATUS)) {
		throw new Error('link: ' + gl.getProgramInfoLog(p));
	}
	return p;
}

// --- renderer ------------------------------------------------------------------

function Renderer(canvas) {
	var gl = canvas.getContext('webgl2', { antialias: true, alpha: false, powerPreference: 'high-performance' });
	if (!gl) { throw new Error('WebGL2 is not available in this browser'); }
	this.gl = gl;
	this.canvas = canvas;

	this.terrainProgram = program(gl, TERRAIN_VS, TERRAIN_FS);
	this.lineProgram = program(gl, LINE_VS, LINE_FS);
	this.uMVP = gl.getUniformLocation(this.terrainProgram, 'uMVP');
	this.uTex = gl.getUniformLocation(this.terrainProgram, 'uTex');
	this.lineMVP = gl.getUniformLocation(this.lineProgram, 'uMVP');
	this.lineColor = gl.getUniformLocation(this.lineProgram, 'uColor');

	this.view = new Float32Array(16);
	this.proj = new Float32Array(16);
	this.mvp = new Float32Array(16);
	this.planes = new Float32Array(24);

	this.chunks = new Map();      // node -> gpu buffers
	this.maxPixelRatio = 2;
	this.drawCalls = 0;
	this.trianglesDrawn = 0;
	this.showLines = false;

	// wireframe box used for the block highlights
	this.lineBuffer = gl.createBuffer();
	this.lineVao = gl.createVertexArray();
	gl.bindVertexArray(this.lineVao);
	gl.bindBuffer(gl.ARRAY_BUFFER, this.lineBuffer);
	var aPosLine = gl.getAttribLocation(this.lineProgram, 'aPos');
	gl.enableVertexAttribArray(aPosLine);
	gl.vertexAttribPointer(aPosLine, 3, gl.FLOAT, false, 0, 0);
	gl.bindVertexArray(null);

	gl.clearColor(SKY[0], SKY[1], SKY[2], 1);
	gl.enable(gl.DEPTH_TEST);
	gl.enable(gl.CULL_FACE);
	gl.cullFace(gl.BACK);
	gl.frontFace(gl.CCW);
}

Renderer.prototype.setTexture = function (image) {
	var gl = this.gl;
	var tex = gl.createTexture();
	gl.bindTexture(gl.TEXTURE_2D, tex);
	gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, false);
	gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
	this.texture = tex;
};

/** upload (or replace) the mesh of a chunk */
Renderer.prototype.uploadChunk = function (node, mesh) {
	var gl = this.gl;
	this.dropChunk(node);
	if (!mesh) { return; }
	var vao = gl.createVertexArray();
	var vbo = gl.createBuffer();
	var ibo = gl.createBuffer();
	gl.bindVertexArray(vao);
	gl.bindBuffer(gl.ARRAY_BUFFER, vbo);
	gl.bufferData(gl.ARRAY_BUFFER, mesh.vertices, gl.STATIC_DRAW);
	var stride = 6 * 4;
	var aPos = gl.getAttribLocation(this.terrainProgram, 'aPos');
	var aUV = gl.getAttribLocation(this.terrainProgram, 'aUV');
	var aLight = gl.getAttribLocation(this.terrainProgram, 'aLight');
	gl.enableVertexAttribArray(aPos);
	gl.vertexAttribPointer(aPos, 3, gl.FLOAT, false, stride, 0);
	gl.enableVertexAttribArray(aUV);
	gl.vertexAttribPointer(aUV, 2, gl.FLOAT, false, stride, 12);
	gl.enableVertexAttribArray(aLight);
	gl.vertexAttribPointer(aLight, 1, gl.FLOAT, false, stride, 20);
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, ibo);
	gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, mesh.indices, gl.STATIC_DRAW);
	gl.bindVertexArray(null);
	var t = POW2[node.J];
	this.chunks.set(node, {
		vao: vao, vbo: vbo, ibo: ibo, count: mesh.indices.length,
		x0: node.x * t, y0: node.y * t, z0: node.z * t,
		x1: (node.x + 1) * t, y1: (node.y + 1) * t, z1: (node.z + 1) * t,
		bytes: mesh.vertices.byteLength + mesh.indices.byteLength
	});
};

Renderer.prototype.dropChunk = function (node) {
	var c = this.chunks.get(node);
	if (!c) { return; }
	var gl = this.gl;
	gl.deleteVertexArray(c.vao);
	gl.deleteBuffer(c.vbo);
	gl.deleteBuffer(c.ibo);
	this.chunks.delete(node);
};

Renderer.prototype.dropAll = function () {
	var self = this;
	Array.from(this.chunks.keys()).forEach(function (n) { self.dropChunk(n); });
};

Renderer.prototype.gpuBytes = function () {
	var total = 0;
	this.chunks.forEach(function (c) { total += c.bytes; });
	return total;
};

Renderer.prototype.resize = function () {
	var dpr = Math.min(window.devicePixelRatio || 1, this.maxPixelRatio);
	var w = Math.floor(this.canvas.clientWidth * dpr);
	var h = Math.floor(this.canvas.clientHeight * dpr);
	if (this.canvas.width !== w || this.canvas.height !== h) {
		this.canvas.width = w;
		this.canvas.height = h;
	}
};

Renderer.prototype.render = function (player, highlights) {
	var gl = this.gl;
	this.resize();
	var w = this.canvas.width, h = this.canvas.height;
	gl.viewport(0, 0, w, h);
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

	var s = player.sight();
	mat4Perspective(this.proj, 70, w / h, 0.15, 8000);
	mat4LookAt(this.view, player.x, player.y, player.z,
		player.x + s[0], player.y + s[1], player.z + s[2], 0, 0, 1);
	mat4Mul(this.mvp, this.proj, this.view);
	frustumPlanes(this.mvp, this.planes);

	gl.useProgram(this.terrainProgram);
	gl.uniformMatrix4fv(this.uMVP, false, this.mvp);
	gl.activeTexture(gl.TEXTURE0);
	gl.bindTexture(gl.TEXTURE_2D, this.texture);
	gl.uniform1i(this.uTex, 0);

	var planes = this.planes;
	var calls = 0, tris = 0;
	this.chunks.forEach(function (c) {
		if (boxOutsideFrustum(planes, c.x0, c.y0, c.z0, c.x1, c.y1, c.z1)) { return; }
		gl.bindVertexArray(c.vao);
		gl.drawElements(gl.TRIANGLES, c.count, gl.UNSIGNED_INT, 0);
		calls++;
		tris += c.count / 3;
	});
	this.drawCalls = calls;
	this.trianglesDrawn = tris;
	gl.bindVertexArray(null);

	// the blocks the player is aiming at
	if (highlights && highlights.length) {
		gl.useProgram(this.lineProgram);
		gl.uniformMatrix4fv(this.lineMVP, false, this.mvp);
		gl.bindVertexArray(this.lineVao);
		for (var i = 0; i < highlights.length; i++) {
			var hl = highlights[i];
			gl.uniform3fv(this.lineColor, hl.color);
			gl.bindBuffer(gl.ARRAY_BUFFER, this.lineBuffer);
			gl.bufferData(gl.ARRAY_BUFFER, wireBox(hl.block), gl.DYNAMIC_DRAW);
			gl.drawArrays(gl.LINES, 0, 24);
		}
		gl.bindVertexArray(null);
	}
};

var WIRE = new Float32Array(72);

/** the 12 edges of a block, slightly inflated so that they stay visible */
function wireBox(block) {
	var w = POW2[block.J];
	var pad = 0.02 * (block.J + 1) + 0.01;
	var x = w * block.x - pad, y = w * block.y - pad, z = w * block.z - pad;
	var s = w + 2 * pad;
	var X = x + s, Y = y + s, Z = z + s;
	var e = [
		x, y, z, X, y, z, X, y, z, X, Y, z, X, Y, z, x, Y, z, x, Y, z, x, y, z,
		x, y, Z, X, y, Z, X, y, Z, X, Y, Z, X, Y, Z, x, Y, Z, x, Y, Z, x, y, Z,
		x, y, z, x, y, Z, X, y, z, X, y, Z, X, Y, z, X, Y, Z, x, Y, z, x, Y, Z
	];
	WIRE.set(e);
	return WIRE;
}

if (typeof module !== 'undefined') {
	module.exports = { Renderer: Renderer, mat4Perspective: mat4Perspective, mat4LookAt: mat4LookAt, mat4Mul: mat4Mul, frustumPlanes: frustumPlanes, boxOutsideFrustum: boxOutsideFrustum };
}

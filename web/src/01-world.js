// -----------------------------------------------------------------------------
// SoBlock - world generation
// Port of org.wavecraft.geometry.worldfunction.* (Java) to JavaScript.
// The pseudo random generator is bit-compatible with java.util.Random so that
// the generated world is identical to the one of the original game.
// -----------------------------------------------------------------------------

var MULT = 0x5DEECE66Dn;
var MASK = (1n << 48n) - 1n;

/** bit exact port of java.util.Random (only the methods we need) */
function JavaRandom(seed) {
	this.seed = (BigInt(seed) ^ MULT) & MASK;
}

JavaRandom.prototype.next = function (bits) {
	this.seed = (this.seed * MULT + 0xBn) & MASK;
	// java returns the signed 32 bits int of (seed >>> (48 - bits))
	var v = Number(this.seed >> BigInt(48 - bits));
	return v | 0;
};

JavaRandom.prototype.nextInt = function (bound) {
	if ((bound & -bound) === bound) { // power of 2
		return Number((BigInt(bound) * BigInt(this.next(31))) >> 31n);
	}
	var bits, val;
	do {
		bits = this.next(31);
		val = bits % bound;
	} while (bits - val + (bound - 1) < 0);
	return val;
};

JavaRandom.prototype.nextDouble = function () {
	return ((this.next(26) * 134217728) + this.next(27)) / 9007199254740992; // 2^27 and 2^53
};

/** Perlin-like value noise on a periodic lattice, port of ThreeDimFunctionPerlin */
function Perlin(gener, N, j, minVal, maxVal) {
	this.N = N;
	this.j = j;
	this.minVal = minVal;
	this.maxVal = maxVal;
	this.scaling = Math.pow(2, j);
	this.invScaling = 1 / this.scaling;
	this.perm = Perlin.randomPermutation(N, gener);
	this.noise = Perlin.noise1d(N, gener);
	this.amp = maxVal - minVal;
}

Perlin.randomPermutation = function (N, gener) {
	var p = new Int32Array(N);
	for (var i = 0; i < N; i++) { p[i] = -1; }
	for (var i = 0; i < N; i++) {
		var nextr = gener.nextInt(N);
		while (p[nextr] > -1) {
			nextr++;
			nextr = nextr % N;
		}
		p[nextr] = i;
	}
	return p;
};

Perlin.noise1d = function (N, gener) {
	var noise = new Float64Array(N);
	for (var i = 0; i < N; i++) { noise[i] = gener.nextDouble(); }
	return noise;
};

// N is always a power of two here, so the java periodize() is a bit mask
Perlin.prototype.pseudoRandomInt = function (i, j, k) {
	var m = this.N - 1, perm = this.perm;
	return this.noise[perm[(i + perm[(j + perm[k & m]) & m]) & m]];
};

Perlin.prototype.valueAt = function (x, y, z) {
	var m = this.N - 1, perm = this.perm, noise = this.noise;
	var X = x * this.invScaling, Y = y * this.invScaling, Z = z * this.invScaling;
	var i = Math.floor(X), j = Math.floor(Y), k = Math.floor(Z);
	var dx = X - i, dy = Y - j, dz = Z - k;
	var ex = 1 - dx, ey = 1 - dy, ez = 1 - dz;
	var i0 = i & m, i1 = (i + 1) & m, j0 = j & m, j1 = (j + 1) & m, k0 = k & m, k1 = (k + 1) & m;
	var pk0 = perm[k0], pk1 = perm[k1];
	var pj0k0 = perm[(j0 + pk0) & m], pj1k0 = perm[(j1 + pk0) & m];
	var pj0k1 = perm[(j0 + pk1) & m], pj1k1 = perm[(j1 + pk1) & m];
	var v =
		noise[perm[(i0 + pj0k0) & m]] * ex * ey * ez +
		noise[perm[(i1 + pj0k0) & m]] * dx * ey * ez +
		noise[perm[(i0 + pj1k0) & m]] * ex * dy * ez +
		noise[perm[(i1 + pj1k0) & m]] * dx * dy * ez +
		noise[perm[(i0 + pj0k1) & m]] * ex * ey * dz +
		noise[perm[(i1 + pj0k1) & m]] * dx * ey * dz +
		noise[perm[(i0 + pj1k1) & m]] * ex * dy * dz +
		noise[perm[(i1 + pj1k1) & m]] * dx * dy * dz;
	return this.minVal + this.amp * v;
};

Perlin.prototype.uncertaintyBound = function (J) {
	return (this.j < J) ? this.maxVal - this.minVal : 0;
};

/** multi scale sum of Perlin noises, port of ThreeDimFunctionPerlinMS */
function PerlinMS(octaves) {
	this.octaves = octaves;
	// octaves with a null amplitude contribute nothing : drop them
	this.active = octaves.filter(function (o) { return o.amp !== 0; });
}

/** the default terrain noise : note that the j=3 octave has a null amplitude,
 *  because of the integer division (-1/10) in the original java code. Kept as is
 *  so that the terrain is exactly the one of the original game. */
PerlinMS.terrain = function () {
	var N = 32;
	var gener = new JavaRandom(0);
	return new PerlinMS([
		new Perlin(gener, N, 1, -0.3 / 10, 0.3 / 10),
		new Perlin(gener, N, 3, 0, 0),
		new Perlin(gener, N, 5, -1, 1),
		new Perlin(gener, N, 7, -2, 2)
	]);
};

/** port of ThreeDimFunctionPerlinMS(J, N, minv, maxv, seed) */
PerlinMS.banded = function (J, N, minv, maxv, seed) {
	var gener = new JavaRandom(seed);
	var octaves = [];
	for (var j = 0; j < J; j++) {
		var f = Math.pow(2, j) / (-1 + Math.pow(2, J));
		octaves.push(new Perlin(gener, N, j, f * minv, f * maxv));
	}
	return new PerlinMS(octaves);
};

PerlinMS.prototype.valueAt = function (x, y, z) {
	var v = 0, a = this.active;
	for (var i = 0; i < a.length; i++) {
		v += a[i].valueAt(x, y, z);
	}
	return v;
};

PerlinMS.prototype.uncertaintyBound = function (J) {
	var b = 0;
	for (var i = 0; i < this.octaves.length; i++) {
		b += this.octaves[i].uncertaintyBound(J);
	}
	return b;
};

// --- terran (block material) ids ---------------------------------------------
var Terran = {
	NAT_STONE: 0,
	NAT_GALET: 1,
	NAT_GRASS: 2,
	NAT_DIRT: 3,
	NAT_SAND: 4,
	NAT_SOLIDSAND: 5,
	NAT_GROUNDSNOW: 6,
	NAT_GROUNDICE: 7,
	NAT_ICE: 8,
	NAT_HERBY_ROCK: 9,
	MAN_BRICK: 10,
	MAN_PARQUET: 11,
	MAN_METAL: 12
};
var TERRAN_NAMES = ['NAT_STONE', 'NAT_GALET', 'NAT_GRASS', 'NAT_DIRT', 'NAT_SAND',
	'NAT_SOLIDSAND', 'NAT_GROUNDSNOW', 'NAT_GROUNDICE', 'NAT_ICE', 'NAT_HERBY_ROCK',
	'MAN_BRICK', 'MAN_PARQUET', 'MAN_METAL'];
var TERRAN_LABELS = ['Stone', 'Pebble', 'Grass', 'Dirt', 'Sand', 'Sandstone',
	'Snow', 'Icy ground', 'Ice', 'Mossy rock', 'Brick', 'Parquet', 'Metal'];

var Climate = { DESERT: 0, ARTIC: 1, JUNGLE: 2, FOREST: 3 };

function getClimate(humidity, temperature) {
	if ((humidity < 0.5 && temperature < 0.5) || temperature < 0.2) { return Climate.ARTIC; }
	if ((humidity < 0.5 && temperature > 0.5) || temperature > 0.8) { return Climate.DESERT; }
	if (humidity > 0.5 && temperature < 0.5) { return Climate.FOREST; }
	return Climate.JUNGLE;
}

/**
 * The world function : a signed "altitude" field (negative = ground) plus the
 * material at a given block. Port of ThreeDimFunctionNoisyFlat +
 * ThreeDimContentBiomeClean, as assembled by WorldFunctionBuilder.
 */
function WorldFunction(z0, deltaz) {
	this.z0 = z0;
	this.deltaz = deltaz;
	this.noise = PerlinMS.terrain();
	this.humidityRandomness = PerlinMS.banded(7, 256, 0, 1, 0);
	this.temperatureRandomness = PerlinMS.banded(7, 256, 0, 1, 1234567890);
	this.soilRandomness = PerlinMS.banded(3, 256, 0, 1, 23456789);
	this.depthGroundSubsoilInterface = 20;
	this.zMin = z0 - deltaz / 2;
	this.zMax = z0 + deltaz / 2;
}

WorldFunction.prototype.valueAt = function (x, y, z) {
	return z + this.deltaz * this.noise.valueAt(x, y, z) - this.z0;
};

WorldFunction.prototype.uncertaintyBound = function (J) {
	return this.deltaz * this.noise.uncertaintyBound(J);
};

/** material of a block, given its center */
WorldFunction.prototype.contentAt = function (cx, cy, cz) {
	if (this.valueAt(cx, cy, cz) < -this.depthGroundSubsoilInterface) {
		// deep underground
		return (this.soilRandomness.valueAt(cx, cy, cz) < 0.8) ? Terran.NAT_STONE : Terran.NAT_SAND;
	}
	// near the surface : pick a material according to the local climate
	var humidity = this.humidityRandomness.valueAt(cx, cy, cz);
	var relativeAltitude = (cz - this.zMin) / (this.zMax - this.zMin);
	var temperature = 0.5 * this.temperatureRandomness.valueAt(cx, cy, cz) + 0.5 * (1 - relativeAltitude);
	var d = this.soilRandomness.valueAt(cx, cy, cz);
	switch (getClimate(humidity, temperature)) {
		case Climate.ARTIC:
			return (d < 0.4) ? Terran.NAT_GROUNDSNOW : (d < 0.6 ? Terran.NAT_GROUNDICE : Terran.NAT_ICE);
		case Climate.DESERT:
			return (d < 0.5) ? Terran.NAT_SAND : Terran.NAT_SOLIDSAND;
		case Climate.JUNGLE:
		case Climate.FOREST:
			return (d < 0.5) ? Terran.NAT_GRASS : Terran.NAT_HERBY_ROCK;
	}
	return Terran.NAT_STONE;
};

if (typeof module !== 'undefined') {
	module.exports = { JavaRandom: JavaRandom, Perlin: Perlin, PerlinMS: PerlinMS, WorldFunction: WorldFunction, Terran: Terran, TERRAN_NAMES: TERRAN_NAMES };
}

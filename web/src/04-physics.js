// -----------------------------------------------------------------------------
// SoBlock - physics, collisions and block picking
// Port of PhysicsWalkIntersect, PhysicsFreeFlightIntersect, CollisionDetection
// and BlocktreeGrabber. Distances are in blocks, times in milliseconds.
// -----------------------------------------------------------------------------

var PLAYER_BOX_MIN = [-0.5, -0.5, -2];   // relative to the eye
var PLAYER_BOX_MAX = [0.5, 0.5, 0.5];

function Player() {
	this.x = 0; this.y = 0; this.z = 0;
	this.theta = 0;   // yaw
	this.phi = 0;     // pitch
	this.vx = 0; this.vy = 0; this.vz = 0;
}

Player.prototype.sight = function () {
	var cp = Math.cos(this.phi);
	return [Math.cos(this.theta) * cp, Math.sin(this.theta) * cp, Math.sin(this.phi)];
};

/** all solid nodes intersecting an axis aligned box */
function intersectedLeaves(root, bx0, by0, bz0, bx1, by1, bz1, out) {
	var stack = [root];
	while (stack.length) {
		var node = stack.pop();
		var st = node.state;
		if (st === DEAD_AIR) { continue; }
		var t = POW2[node.J];
		var nx0 = node.x * t, ny0 = node.y * t, nz0 = node.z * t;
		var nx1 = nx0 + t, ny1 = ny0 + t, nz1 = nz0 + t;
		var hit = !(bx1 < nx0 || nx1 < bx0 || by1 < ny0 || ny1 < by0 || bz1 < nz0 || nz1 < bz0) ||
			(bx0 < nx0 && bx1 > nx1 && by0 < ny0 && by1 > ny1 && bz0 < nz0 && bz1 > nz1);
		if (!hit) { continue; }
		if (st === DEAD_GROUND || st === LEAF) {
			out.push(node);
		} else if (node.sons) {
			for (var o = 0; o < 8; o++) { stack.push(node.sons[o]); }
		}
	}
	return out;
}

// --- walk mode ----------------------------------------------------------------

function PhysicsWalk() {
	this.velocity = [0, 0, 0];
	this.velocityNm1 = [0, 0, 0];
	this.alphaFrictionGround = 400 / 1000;
	this.alphaFrictionAir = 20 / 1000;
	this.gravityG = 2 * 9.81 / 1000 / 1000;
	this.nonLinFriction = 0.002;
	this.jumpCoefft = Math.sqrt(2 * 9.81 / 1000 / 1000 * 1.1) * 1000;
	this.shockCoeff = 0.5;
	this.bounceCoeff = 0.3;
	this.vMinBounce = 2;
	this.scalarSpeedDefault = 0.005;
}

PhysicsWalk.prototype.reset = function () { this.velocity = [0, 0, 0]; };

PhysicsWalk.prototype.move = function (player, dt, dirs, speedMult, root) {
	var v = this.velocity;
	var scalarSpeed = speedMult * this.scalarSpeedDefault;
	// on foot we walk in the horizontal plane, at a speed that does not depend
	// on how far up or down we are looking
	var ux = Math.cos(player.theta), uy = Math.sin(player.theta);
	var tx = 0, ty = 0, tz = 0;
	if (dirs.forward) { tx += scalarSpeed * ux; ty += scalarSpeed * uy; }
	if (dirs.backward) { tx -= scalarSpeed * ux; ty -= scalarSpeed * uy; }
	if (dirs.left) { tx -= scalarSpeed * uy; ty += scalarSpeed * ux; }
	if (dirs.right) { tx += scalarSpeed * uy; ty -= scalarSpeed * ux; }
	if (dirs.up && v[2] === 0) { tz += this.jumpCoefft * scalarSpeed; }

	// accelerate towards the target velocity, less in the air than on the ground
	if (v[2] === 0) {
		var a = this.alphaFrictionGround;
		v[0] += (tx - v[0]) * a; v[1] += (ty - v[1]) * a; v[2] += (tz - v[2]) * a;
	} else {
		var b = this.alphaFrictionAir;
		var saveVz = v[2];
		v[0] += (tx - v[0]) * b; v[1] += (ty - v[1]) * b;
		v[2] = saveVz;
	}
	v[2] -= this.gravityG * dt;
	v[2] -= v[2] * Math.abs(v[2]) * this.nonLinFriction * dt;

	// collect the blocks on the way and slide along them
	var box = playerBoxExtruded(player, v, dt);
	var leaves = intersectedLeaves(root, box[0], box[1], box[2], box[3], box[4], box[5], []);
	this.velocityNm1[0] = v[0]; this.velocityNm1[1] = v[1]; this.velocityNm1[2] = v[2];
	this.avoidBlocks(leaves, player, dt, false, false, 0);
};

function playerBoxExtruded(player, v, dt) {
	var x0 = player.x + PLAYER_BOX_MIN[0], y0 = player.y + PLAYER_BOX_MIN[1], z0 = player.z + PLAYER_BOX_MIN[2];
	var x1 = player.x + PLAYER_BOX_MAX[0], y1 = player.y + PLAYER_BOX_MAX[1], z1 = player.z + PLAYER_BOX_MAX[2];
	var dx = v[0] * dt, dy = v[1] * dt, dz = v[2] * dt;
	return [
		Math.min(x0, x0 + dx), Math.min(y0, y0 + dy), Math.min(z0, z0 + dz),
		Math.max(x1, x1 + dx), Math.max(y1, y1 + dy), Math.max(z1, z1 + dz)
	];
}

PhysicsWalk.prototype.avoidBlocks = function (leaves, player, dt, shockZp, shockZm, count) {
	if (count > 8) { return; }
	var v = this.velocity;
	var playerWidth = 0.15, playerHeightDown = 1.5, playerHeightUp = 0.30;
	var dtSave = dt;
	var isBlockedX = false, isBlockedY = false, isBlockedZ = false;
	var epsDt = 1E-3, epsVel = 1E-15, eps = 1E-3;
	if (Math.abs(v[0]) < epsVel) { v[0] = 0; }
	if (Math.abs(v[1]) < epsVel) { v[1] = 0; }
	if (Math.abs(v[2]) < epsVel) { v[2] = 0; }
	var ux = v[0], uy = v[1], uz = v[2];
	var eyex = player.x, eyey = player.y, eyez = player.z;

	for (var i = 0; i < leaves.length; i++) {
		var leaf = leaves[i];
		var size = POW2[leaf.J];
		var cxMin = leaf.x * size, cyMin = leaf.y * size, czMin = leaf.z * size;
		var cxMax = cxMin + size, cyMax = cyMin + size, czMax = czMin + size;
		var t, xf, yf, zf, tx, ty, tz;

		if (ux > epsVel && eyex <= cxMin - playerWidth && !isBlockedX) {
			t = (cxMin - playerWidth - eyex) / ux;
			yf = eyey + t * uy; zf = eyez + t * uz;
			if (cyMin - playerWidth <= yf && yf <= cyMax + playerWidth &&
				czMin - playerHeightUp <= zf && zf <= czMax + playerHeightDown) {
				tx = Math.max(t - eps / ux, 0);
				if (tx < epsDt) { isBlockedX = true; }
				dt = Math.min(dt, tx);
			}
		}
		if (ux < -epsVel && eyex >= cxMax + playerWidth && !isBlockedX) {
			t = (cxMax + playerWidth - eyex) / ux;
			yf = eyey + t * uy; zf = eyez + t * uz;
			if (cyMin - playerWidth <= yf && yf <= cyMax + playerWidth &&
				czMin - playerHeightUp <= zf && zf <= czMax + playerHeightDown) {
				tx = Math.max(t + eps / ux, 0);
				if (tx < epsDt) { isBlockedX = true; }
				dt = Math.min(dt, tx);
			}
		}
		if (uy > epsVel && eyey <= cyMin - playerWidth && !isBlockedY) {
			t = (cyMin - playerWidth - eyey) / uy;
			xf = eyex + t * ux; zf = eyez + t * uz;
			if (cxMin - playerWidth <= xf && xf <= cxMax + playerWidth &&
				czMin - playerHeightUp <= zf && zf <= czMax + playerHeightDown) {
				ty = Math.max(t - eps / uy, 0);
				if (ty < epsDt) { isBlockedY = true; }
				dt = Math.min(dt, ty);
			}
		}
		if (uy < -epsVel && eyey >= cyMax + playerWidth && !isBlockedY) {
			t = (cyMax + playerWidth - eyey) / uy;
			xf = eyex + t * ux; zf = eyez + t * uz;
			if (cxMin - playerWidth <= xf && xf <= cxMax + playerWidth &&
				czMin - playerHeightUp <= zf && zf <= czMax + playerHeightDown) {
				ty = Math.max(t + eps / uy, 0);
				if (ty < epsDt) { isBlockedY = true; }
				dt = Math.min(dt, ty);
			}
		}
		if (uz > epsVel && eyez <= czMin - playerHeightUp && !isBlockedZ) {
			t = (czMin - playerHeightUp - eyez) / uz;
			xf = eyex + t * ux; yf = eyey + t * uy;
			if (cyMin - playerWidth <= yf && yf <= cyMax + playerWidth &&
				cxMin - playerWidth <= xf && xf <= cxMax + playerWidth) {
				tz = Math.max(t - eps / uz, 0);
				if (tz < epsDt) { isBlockedZ = true; shockZp = true; }
				dt = Math.min(dt, tz);
			}
		}
		if (uz < -epsVel && eyez >= czMax + playerHeightDown && !isBlockedZ) {
			t = (czMax + playerHeightDown - eyez) / uz;
			xf = eyex + t * ux; yf = eyey + t * uy;
			if (cyMin - playerWidth <= yf && yf <= cyMax + playerWidth &&
				cxMin - playerWidth <= xf && xf <= cxMax + playerWidth) {
				tz = Math.max(t + eps / uz, 0);
				if (tz < epsDt) { isBlockedZ = true; shockZm = true; }
				dt = Math.min(dt, tz);
			}
		}
	}

	if (Math.abs(dt) > epsDt) {
		player.x += ux * dt; player.y += uy * dt; player.z += uz * dt;
		v[0] = ux; v[1] = uy; v[2] = uz;
		if (shockZp) { v[2] = -this.shockCoeff * this.velocityNm1[2]; }
		if (shockZm && this.vMinBounce < -uz) { v[2] = -this.bounceCoeff * this.velocityNm1[2]; }
	} else {
		if (isBlockedX) { v[0] = 0; }
		if (isBlockedY) { v[1] = 0; }
		if (isBlockedZ) { v[2] = 0; }
		if (dtSave > epsDt && !(isBlockedX && isBlockedY && isBlockedZ)) {
			this.avoidBlocks(leaves, player, dtSave, shockZp, shockZm, count + 1);
		}
	}
};

// --- free flight mode ---------------------------------------------------------

function PhysicsFly() {
	this.scalarSpeedDefault = 0.005;
}

PhysicsFly.prototype.move = function (player, dt, dirs, speedMult, root) {
	// the position is updated with the speed of the previous frame, as in the
	// original game
	player.x += player.vx * dt; player.y += player.vy * dt; player.z += player.vz * dt;
	var cp = Math.cos(player.phi);
	var ux = Math.cos(player.theta) * cp, uy = Math.sin(player.theta) * cp, uz = Math.sin(player.phi);
	var s = speedMult * this.scalarSpeedDefault;
	var vx = 0, vy = 0, vz = 0;
	if (dirs.forward) { vx += s * ux; vy += s * uy; vz += s * uz; }
	if (dirs.backward) { vx -= s * ux; vy -= s * uy; vz -= s * uz; }
	if (dirs.left) { vx -= s * uy; vy += s * ux; }
	if (dirs.right) { vx += s * uy; vy -= s * ux; }
	if (dirs.up) { vz += s; }
	if (dirs.down) { vz -= s; }
	player.vx = vx; player.vy = vy; player.vz = vz;

	// When the eye itself ends up inside a block - after flying fast into a
	// hill - collisions are skipped, so that one can always fly back out
	// instead of being stuck in the ground for good.
	var eyeCell = root.smallestCellContaining(Math.floor(player.x), Math.floor(player.y), Math.floor(player.z), 0);
	if (eyeCell && (eyeCell.state === LEAF || eyeCell.state === DEAD_GROUND)) { return; }

	// push the player out of the blocks it ended up inside of
	var x0 = player.x + PLAYER_BOX_MIN[0], y0 = player.y + PLAYER_BOX_MIN[1], z0 = player.z + PLAYER_BOX_MIN[2];
	var x1 = player.x + PLAYER_BOX_MAX[0], y1 = player.y + PLAYER_BOX_MAX[1], z1 = player.z + PLAYER_BOX_MAX[2];
	var leaves = intersectedLeaves(root, x0, y0, z0, x1, y1, z1, []);
	var done = [false, false, false, false, false, false];
	for (var i = 0; i < leaves.length; i++) {
		var leaf = leaves[i];
		var t = POW2[leaf.J];
		var bx0 = leaf.x * t, by0 = leaf.y * t, bz0 = leaf.z * t;
		var bx1 = bx0 + t, by1 = by0 + t, bz1 = bz0 + t;
		var d;
		if (!done[0]) {
			d = z0 - bz1;
			if (d < 0 && z0 - bz0 > 0) { player.z += -0.001 - d; done[0] = true; done[1] = true; }
		}
		if (!done[1]) {
			d = z1 - bz0;
			if (d > 0 && z1 - bz1 < 0) { player.z -= d; done[1] = true; done[0] = true; }
		}
		if (!done[2]) {
			d = x0 - bx1;
			if (d < 0 && x0 - bx0 > 0) { player.x -= d; done[2] = true; done[3] = true; }
		}
		if (!done[3]) {
			d = x1 - bx0;
			if (d > 0 && x1 - bx1 < 0) { player.x -= d; done[3] = true; }
		}
		if (!done[4]) {
			d = y0 - by1;
			if (d < 0 && y0 - by0 > 0) { player.y -= d; done[4] = true; }
		}
		if (!done[5]) {
			d = y1 - by0;
			if (d > 0 && y1 - by1 < 0) { player.y -= d; done[5] = true; }
		}
	}
};

// --- picking ------------------------------------------------------------------

var FAR_AWAY = 10E20;

/** distance to the nearest face of a block hit by a ray, FAR_AWAY if missed */
function blockRayDistance(x, y, z, J, ox, oy, oz, dx, dy, dz, wantFace) {
	var t = POW2[J];
	var bx0 = x * t, by0 = y * t, bz0 = z * t;
	var bx1 = bx0 + t, by1 = by0 + t, bz1 = bz0 + t;
	var dmin = FAR_AWAY, nmin = 0, d, px, py, pz;
	// the six faces, exactly as Face.intersectionSignedDistance does it
	if (Math.abs(dx) > 10E-5) {
		d = (bx0 - ox) / dx;
		if (d >= 0) {
			py = oy + d * dy; pz = oz + d * dz;
			if (by0 < py && py < by1 && bz0 < pz && pz < bz1 && d < dmin) { dmin = d; nmin = -1; }
		}
		d = (bx1 - ox) / dx;
		if (d >= 0) {
			py = oy + d * dy; pz = oz + d * dz;
			if (by0 < py && py < by1 && bz0 < pz && pz < bz1 && d < dmin) { dmin = d; nmin = 1; }
		}
	}
	if (Math.abs(dy) > 10E-5) {
		d = (by0 - oy) / dy;
		if (d >= 0) {
			px = ox + d * dx; pz = oz + d * dz;
			if (bx0 < px && px < bx1 && bz0 < pz && pz < bz1 && d < dmin) { dmin = d; nmin = -2; }
		}
		d = (by1 - oy) / dy;
		if (d >= 0) {
			px = ox + d * dx; pz = oz + d * dz;
			if (bx0 < px && px < bx1 && bz0 < pz && pz < bz1 && d < dmin) { dmin = d; nmin = 2; }
		}
	}
	if (Math.abs(dz) > 10E-5) {
		d = (bz0 - oz) / dz;
		if (d >= 0) {
			px = ox + d * dx; py = oy + d * dy;
			if (bx0 < px && px < bx1 && by0 < py && py < by1 && d < dmin) { dmin = d; nmin = -3; }
		}
		d = (bz1 - oz) / dz;
		if (d >= 0) {
			px = ox + d * dx; py = oy + d * dy;
			if (bx0 < px && px < bx1 && by0 < py && py < by1 && d < dmin) { dmin = d; nmin = 3; }
		}
	}
	return wantFace ? nmin : dmin;
}

/** nearest visible block hit by the ray, or null */
function nearestIntersectedLeaf(root, ox, oy, oz, dx, dy, dz) {
	var best = null, bestD = FAR_AWAY;
	var stack = [root];
	while (stack.length) {
		var node = stack.pop();
		if (node.state === DEAD_AIR) { continue; }
		var d = blockRayDistance(node.x, node.y, node.z, node.J, ox, oy, oz, dx, dy, dz, false);
		if (d >= FAR_AWAY) { continue; }
		if (node.sons) {
			for (var o = 0; o < 8; o++) { stack.push(node.sons[o]); }
		} else if (node.state === LEAF && d < bestD) {
			bestD = d; best = node;
		}
	}
	return best;
}

/** ancestor of a block at scale J */
function ancestorAt(x, y, z, J, targetJ) {
	if (targetJ < J) { return null; }
	var div = POW2[targetJ - J];
	return { x: Math.floor(x / div), y: Math.floor(y / div), z: Math.floor(z / div), J: targetJ };
}

/**
 * What the player is aiming at : the block to remove, and the block that would
 * be created in front of it, both grown to the current build size.
 */
function pickBlocks(root, player, targetJ) {
	var s = player.sight();
	var leaf = nearestIntersectedLeaf(root, player.x, player.y, player.z, s[0], s[1], s[2]);
	if (!leaf) { return { remove: null, add: null }; }
	var normal = blockRayDistance(leaf.x, leaf.y, leaf.z, leaf.J, player.x, player.y, player.z, s[0], s[1], s[2], true);
	if (normal === 0) { return { remove: null, add: null }; }
	var remove = ancestorAt(leaf.x, leaf.y, leaf.z, leaf.J, targetJ);
	// the block in front of the hit face
	var fx = leaf.x, fy = leaf.y, fz = leaf.z;
	if (normal === 1) { fx += 1; } else if (normal === -1) { fx -= 1; }
	else if (normal === 2) { fy += 1; } else if (normal === -2) { fy -= 1; }
	else if (normal === 3) { fz += 1; } else if (normal === -3) { fz -= 1; }
	var add = ancestorAt(fx, fy, fz, leaf.J, targetJ);
	if (add) {
		// never build inside the player
		var t = POW2[add.J];
		var ax0 = add.x * t, ay0 = add.y * t, az0 = add.z * t;
		var px0 = player.x + PLAYER_BOX_MIN[0], py0 = player.y + PLAYER_BOX_MIN[1], pz0 = player.z + PLAYER_BOX_MIN[2];
		var px1 = player.x + PLAYER_BOX_MAX[0], py1 = player.y + PLAYER_BOX_MAX[1], pz1 = player.z + PLAYER_BOX_MAX[2];
		if (!(px1 < ax0 || ax0 + t < px0 || py1 < ay0 || ay0 + t < py0 || pz1 < az0 || az0 + t < pz0)) {
			add = null;
		}
	}
	var maxCoord = POW2[JMAX - targetJ];
	if (add && (add.x < 0 || add.y < 0 || add.z < 0 || add.x >= maxCoord || add.y >= maxCoord || add.z >= maxCoord)) {
		add = null;
	}
	return { remove: remove, add: add, leaf: leaf, normal: normal };
}

if (typeof module !== 'undefined') {
	module.exports = { Player: Player, PhysicsWalk: PhysicsWalk, PhysicsFly: PhysicsFly, pickBlocks: pickBlocks };
}

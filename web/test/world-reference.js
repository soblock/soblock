#!/usr/bin/env node
// Checks the javascript world generator against values produced by the original
// java code (see Ref.java, and reference.txt for its output).
const fs = require('fs');
const path = require('path');
const dir = path.join(__dirname, '..', 'src');

const src = ['01-world.js'].map(f => fs.readFileSync(path.join(dir, f), 'utf8')).join('\n');
const { WorldFunction, TERRAN_NAMES } = new Function(src + '\n; return {WorldFunction, TERRAN_NAMES};')();

const wf = new WorldFunction(Math.pow(2, 11), 50);
let checked = 0, bad = 0;
for (const line of fs.readFileSync(path.join(__dirname, 'reference.txt'), 'utf8').trim().split('\n')) {
	const p = line.split(' ');
	checked++;
	if (p[0] === 'V') {                       // the terrain field at a point
		const v = wf.valueAt(+p[1], +p[2], +p[3]);
		if (Math.abs(v - +p[4]) > 1e-9) { bad++; console.log('value mismatch', line, v); }
	} else if (p[0] === 'C') {                // the material of a unit block
		const t = TERRAN_NAMES[wf.contentAt(+p[1] + 0.5, +p[2] + 0.5, +p[3] + 0.5)];
		if (t !== p[4]) { bad++; console.log('material mismatch', line, t); }
	} else if (p[0] === 'U') {                // the uncertainty bound at a scale
		const u = wf.uncertaintyBound(+p[1]);
		if (Math.abs(u - +p[2]) > 1e-9) { bad++; console.log('bound mismatch', line, u); }
	}
}
console.log(checked + ' reference values checked, ' + bad + ' mismatches');
process.exit(bad ? 1 : 0);

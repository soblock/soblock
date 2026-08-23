#!/usr/bin/env node
// Bundles web/src/*.js, web/src/page.html and the terrain texture into
//   web/index.html    a single self contained page, playable from a file:// url
//   web/artifact.html the same page as a fragment, for hosts that supply the
//                     <html> / <head> / <body> skeleton themselves
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const srcDir = path.join(__dirname, 'src');

const files = fs.readdirSync(srcDir).filter(f => /^\d.*\.js$/.test(f)).sort();
const js = files.map(f => {
	const body = fs.readFileSync(path.join(srcDir, f), 'utf8')
		// the node exports at the end of each file are only there for the tests
		.replace(/\nif \(typeof module !== 'undefined'\) \{[\s\S]*?\n\}\n?$/, '\n');
	return '// ===== ' + f + ' =====\n' + body;
}).join('\n');

const png = fs.readFileSync(path.join(root, 'data', 'terrain4.png'));
const textureJs = 'var TERRAIN_TEXTURE_DATA_URI = "data:image/png;base64,' + png.toString('base64') + '";';

const fragment = fs.readFileSync(path.join(srcDir, 'page.html'), 'utf8')
	.replace('/*__TERRAIN_TEXTURE__*/', () => textureJs)
	.replace('/*__GAME_JS__*/', () => js);

const page = '<!DOCTYPE html>\n<html lang="en">\n<head>\n' +
	'<meta charset="utf-8">\n' +
	'<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">\n' +
	fragment.slice(0, fragment.indexOf('</style>') + 9) +
	'</head>\n<body>\n' +
	fragment.slice(fragment.indexOf('</style>') + 9) +
	'</body>\n</html>\n';

write(path.join(__dirname, 'index.html'), page);
write(path.join(__dirname, 'artifact.html'), fragment);

function write(file, content) {
	fs.writeFileSync(file, content);
	console.log('wrote ' + path.relative(root, file) + ' (' + (content.length / 1024).toFixed(0) + ' kB)');
}

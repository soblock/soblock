# SoBlock in the browser

A standalone web version of SoBlock : the whole game — sparse voxel octree,
level of detail, terrain generator, physics, building and digging with blocks of
any size — ported from the original Java / LWJGL code in `src/` to JavaScript
and WebGL2.

**Play it** by opening `web/index.html` in Chrome (double clicking the file
works, no server needed), or by dropping that single file on any static host.
Everything is inside it : code, texture atlas and menus, one file.

On a phone or a tablet it switches to thumb controls by itself. On an iPhone,
open the page in Safari and use Share → Add to Home Screen : it then runs
without the browser bars, which is worth the two taps.

## Controls

| Key | Action |
| --- | --- |
| `W` `A` `S` `D` | move |
| `Space` | jump, or fly up |
| `C` | fly down |
| `Shift`, or `R` `T` `Y` `U` | speed ×4, or ×4 ×16 ×64 ×256 |
| `F` | switch between walking and free flight |
| left click / `Enter` | place a block |
| right click / `Backspace` | dig a block |
| `Z` / `X` or the mouse wheel | bigger / smaller build size, from 1 to 1024 |
| `1` … `6` | pick a material |
| `L` / `\` | more / less level of detail |
| `O` | show the octree chunks |
| `H` | show the debug stats |
| `Esc` | menu (save, load, options) |

## On a touch screen

The thumb controls appear on their own on a touch device, and the game keeps a
smaller budget there : it starts at 700 chunks instead of 1600 and stops
growing at 1500, and draws at most 1.5 device pixels per css pixel.

| Control | Action |
| --- | --- |
| left thumb | walk — the stick appears wherever you touch the left of the screen |
| right thumb | drag to look around |
| ▲ / ▼ | jump or fly up, fly down |
| ⛏ / ▣ | dig, build — hold to repeat |
| − 1 + | build size, from 1 to 1024 |
| Fly / Walk | switch between the two |
| ☰ | menu, since a phone has no escape key |
| the material row | tap a block to pick it |

Aiming is the crosshair in the middle of the screen, as on a desktop : turn with
the right thumb until the block you want is under it, then dig or build.

Worlds are saved in the browser local storage, as the list of edits that were
made to the generated world — exactly what `GameSave` stores in the Java
version.

## How it is put together

| file | what it holds | ported from |
| --- | --- | --- |
| `src/01-world.js` | terrain field, biomes, materials, `java.util.Random` | `geometry.worldfunction.*` |
| `src/02-blocktree.js` | the octree, the edit octree, the level of detail rules | `geometry.blocktree.*`, `…blocktree.modif.*` |
| `src/03-mesh.js` | faces of a chunk, ambient occlusion, texture atlas | `graphics.rasterizer`, `graphics.vbo.*`, `graphics.texture.MegaTexture` |
| `src/04-physics.js` | walking, free flight, collisions, block picking | `gameobject.physics.*`, `…modif.BlocktreeGrabber` |
| `src/05-render.js` | WebGL2 renderer, one buffer per chunk, frustum culling | `graphics.GraphicEngine`, `graphics.vbo.VBOBlocktreePool` |
| `src/06-game.js` | the game loop, the edits, the background work scheduler | `gameobject.GameEngine`, `geometry.blocktree.BlocktreeRefiner` |
| `src/07-ui.js` | input, heads up display, menus, saved games | `ui.*`, `graphics.hud.*` |
| `src/page.html` | markup and style of the page | `ui.menu.twl.*` |

`node build.js` bundles those into two files :

* `index.html` — the standalone page described above;
* `artifact.html` — the same content without the `<html>` / `<head>` / `<body>`
  skeleton, for hosts that provide it themselves.

Both are committed, so there is nothing to build in order to play.

## Same world as the original

The terrain is not a look-alike : the noise generator is a bit exact port of
`java.util.Random`, so every octave, every permutation table and therefore every
hill and every biome is identical to the Java game. `test/world-reference.js`
checks the javascript against values produced by the original code
(`test/Ref.java`, output in `test/reference.txt`) :

```
node web/test/world-reference.js      # 52 reference values, 0 mismatches
node web/test/edits.js                # digging and building, no browser needed
node web/test/budget.js               # the budget follows the machine, no browser needed
node web/test/smoke.js                # drives the real page in headless chromium
node web/test/touch.js                # the same page on an emulated phone
```

`edits.js` runs the game logic against a stub renderer, so a world of edits at
every build size can be checked in a few seconds. After each dig and each build
it asks that every chunk being drawn is still part of the octree, and that every
chunk's mesh matches what a fresh build of it would produce — a chunk that has
been replaced but is still drawn leaves its faces hanging in the air for good.

`budget.js` uses the same stub renderer to drive the budget controller with made
up frame costs : cheap frames have to buy a bigger budget and turn it into a
wider detail radius, expensive ones have to hand it back, and the ceiling has to
hold.

The two page tests need `playwright`. `smoke.js` checks that a world generates,
that the player falls and lands, walks, flies, digs, builds, saves and reloads,
that no holes open at the seams between levels of detail, and that the level of
detail keeps following the player once the budget is spent. `touch.js` loads
the page in a phone sized viewport with a touch screen and no pointer lock, and
checks the thumb stick, the look drag and every button. Both drive chromium :
they cover the touch behaviour, not Safari itself.

## Deliberate differences

Everything else follows the original closely — the same octree states, the same
`radius × edge / distance` level of detail priority, the same jump, friction and
bounce constants, the same texture tiles per material and per face. Four things
were changed on purpose :

* **Walking speed no longer depends on the pitch.** In the Java code the ground
  speed is multiplied by `cos(phi)`, so looking at your feet while building
  slowed you to a crawl. Free flight still goes where you look.
* **A detail budget.** The Java version had a background thread refining for
  ever, which a browser tab cannot afford, so the web version keeps a budget of
  meshed chunks. It is spent by moving the level of detail radius rather than by
  refusing to split : the radius starts at roughly what the budget can pay for,
  then follows the chunk count, stepping at most 6% a quarter of a second and
  holding still while the count is already moving. Refusing to split instead
  would freeze the whole level of detail as soon as the budget was full —
  nothing could be refined ahead of the player, because nothing far behind had
  crossed its merge threshold yet. The option panel sets the radius the player
  would like; the budget may keep the effective one lower, and the debug stats
  show both.

  The budget itself is not a fixed number : the game measures what it costs the
  machine and moves it. It starts at 1600 chunks (700 on a phone) and, once a
  second, looks at the median time its own work takes in a frame and at how much
  of the js heap is spoken for. Under 6 ms and half the heap it grows the budget
  by 20%, over 12 ms or three quarters of the heap it gives 20% back, and in
  between it holds. It only grows when the player has actually asked for more
  detail than they are getting, the chunks already paid for are on screen and no
  work is queued, so a machine sitting comfortably at its requested radius is
  left alone. A chunk costs about 97 kB of heap, so the ceiling is the smaller of
  8000 chunks and half of `performance.memory.jsHeapSizeLimit` divided by that —
  roughly 780 MB of heap and a detail radius near 9 on a desktop. Browsers
  without `performance.memory` (Safari, Firefox) get the flat 8000. Radii much
  past that are not a setting away : radius 15 is around 21 600 chunks, some
  2 GB of heap, which would need the octree packed into typed arrays rather than
  one object per node.
* **Flying out of the ground.** If the camera ends up inside a block — flying
  fast into a hillside — collisions are skipped until it is out, instead of
  being stuck for good.
* **Progressive start.** The world keeps sharpening for a few seconds after the
  loading screen hands over, rather than making you wait for the whole map.
* **Seams are rebuilt.** A chunk hides the faces it shares with a solid
  neighbour, so those faces depend on how finely the neighbour is cut. The Java
  version rebuilds a chunk when its own level of detail changes but never its
  neighbours (it does rebuild them around an edit), which leaves stale seams you
  can see the sky through. Here the neighbours along the six sides are rebuilt in
  the same batch as the change, so the swap is all or nothing;
  `test/smoke.js` measures the remaining holes by comparing a frame against the
  same frame with backface culling switched off, both while the rebuilds are in
  flight and once they have settled.
* **Thumb controls.** The original is a keyboard and mouse game; the touch
  layer described above has no equivalent in it.

# SoBlock source repository

## SoBlock

SoBlock is a sparse voxel octree block building game. So it's like minecraft, but you can see the whole map (no fog) and build/destroy with arbitrarily large blocks. The blocks that are far away are rendered coarser so that the whole map fit in memory.

Here is a video of what it looks like:
<a href="http://www.youtube.com/watch?feature=player_embedded&v=qdXPuQb8uI4" target="_blank"><img src="http://img.youtube.com/vi/qdXPuQb8uI4/0.jpg" 
alt="IMAGE ALT TEXT HERE" width="240" height="180" border="10" /></a>

It is written in Java and uses LWJGL for interfacing with OpenGL.

## Play it in a browser

`web/index.html` is a standalone port of the game to JavaScript and WebGL2 :
one self contained file, no install, no server — open it in Chrome and play.
It generates exactly the same world as the Java version, and keeps the same
octree, level of detail, physics and building rules. See
[web/README.md](web/README.md) for the controls and for how the port is
organised.

## Install

1. Get Eclipse and Java 1.6 and figure out how to make eclipse to work with Java 1.6.

2. Clone this repo

3. Import project from eclipse with file -> import -> general -> existing project into workspace

4. Laucnh
 
## Get the Game

If you just want to play SoBlock you can download compiled version at

https://sites.google.com/site/soblockgame/download

# DuckHunt
A simple minigame made for BajanCanadian's Fan Battles series.

## Objective
#### Players
Make it to the goal location without being killed by the hunters. When you reach the goal, you will be teleported to the hunter spawn position and given a Sharpness 10 Diamond Sword.

#### Hunters
Eliminate all players before they reach the goal.

THis plugin does not give items for kits, manage player movement (beyond the forcefields), or handle deaths. It is recommended to employ the use of a kit plugin or a kick-on-death plugin when using this plugin.

## Commands

Command | Description
------- | -----------
/newMap (Name) | Creates a new empty map file
/loadMap (Name) | Loads an already existing map file
/unloadMap | Unloads a loaded map
/setPlayerSpawn | Sets the player spawn (non-OPs) to your location
/setHunterSpawn | Sets the hunter spawn (OPs) to your location
/setGoal (Radius) | Sets the goal point, and the radius for players to be within
/setWallPoint1 | Sets point 1 for the wall converter
/setWallPoint2 | Sets point 2 for the wall converter
/setWall (Material) | Converts all of the provided material into invisible forcefields
/setWall clear | Clears specified walls, *does not put back physical blocks*
/warpAll | Warps all players to their associated spawn positions

All commands require OP to use.

## Getting Started

1. Load up a Minecraft world with the map you want
2. Run the command **/newMap (Name)** to create a map file. Map files are saved in **/plugins/DuckHunt/(MapName).json**.
3. Use the associated commands to build your map

Duck Hunt will not handle world switching for you, and only one map can be loaded at a time. When loading another map, you don't need to append .json to the name.

If your server is restarted or reloaded, remember to run **/loadMap**.

# Marloth Kotlin

A 3D Marloth Game created by Christopher W. Johnson

## Overview

* There is also a Marloth book but the rest of this document only refers to Marloth the game
* This codebase is written in Kotlin and 95% functional/immutable
* The main purpose of publishing this repository with an MIT license is to provide an example implementation
* Marloth is currently in early alpha
* Silent Orb may eventually release a commercial version of Marloth
    * At such a point 
## Dependencies

### Repositories

This project depends on two repositories

* [Mythic Game Engine](https://github.com/silentorb/mythic-kotlin)
* [Imp Programming Langauge](https://github.com/silentorb/imp-kotlin)

The Marloth Gradle configuration currently assumes that each repo is cloned alongside the Marloth project and 
named `mythic` and `imp`

#### Example directory structure

* `imp`
* `marloth`
* `mythic`

### Assets

The following programs are needed to run the assets pipeline:

* Blender 2.83+
* Filter Forge 8+
* Python 3.5+

The paths to these files can be configured using the following environment variables:
```dotenv
BLENDER_PATH=/path-to/blender.exe
FILTER_FORGE_PATH=/path-to/FFXCmdRenderer-x64.exe
PYTHON_PATH=/path-to/python.exe
```

Marloth uses `dotenv` for development.  You can place a `.env` file in the root project directory.

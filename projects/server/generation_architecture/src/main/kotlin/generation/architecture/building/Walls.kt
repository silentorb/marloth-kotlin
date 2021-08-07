package generation.architecture.building

import generation.general.Direction
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.quarterAngle

fun directionRotation(direction: Direction): Float =
    when (direction) {
      Direction.east -> 0f
      Direction.north -> quarterAngle
      Direction.west -> Pi
      Direction.south -> Pi * 1.5f
      else -> throw Error("Not supported")
    }

package generation.architecture.definition

import generation.elements.*
import mythic.spatial.Vector3i

private val secondFloor = Vector3i(0, 0, 1)

class PolyominoeDefinitions {
  companion object {

    val singleCellRoom: Polyomino = mapOf(
        Vector3i.zero to BlockDefinitions.singleCellRoom
    )

//    val spiralStairsSingle: Polyomino = mapOf(
//        Vector3i.zero to BlockDefinitions.stairBottom,
//        secondFloor to BlockDefinitions.stairTop
//    )

    val spiralStairsBottom: Polyomino = mapOf(
        Vector3i.zero to BlockDefinitions.stairBottom
    )

    val spiralStairsMiddle: Polyomino = mapOf(
        Vector3i.zero to BlockDefinitions.stairMiddle
    )

    val spiralStairsTop: Polyomino = mapOf(
        Vector3i.zero to BlockDefinitions.stairTop
    )
  }
}

fun allPolyominoes() = enumeratePolyominoes(PolyominoeDefinitions).toSet()

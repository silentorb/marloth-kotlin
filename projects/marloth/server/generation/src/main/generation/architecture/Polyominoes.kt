package generation.architecture

import generation.elements.Polyomino
import generation.elements.Side
import generation.elements.enumeratePolyominoes
import generation.elements.newBlock
import mythic.spatial.Vector3i

private val wall: Side = setOf()
private val doorway: Side = setOf()

class Blocks {
  companion object {

    val singleCellRoom = newBlock(
        top = wall,
        bottom = wall,
        east = doorway,
        north = doorway,
        west = doorway,
        south = doorway
    )

  }
}

class PolyominoeDefinitions {
  companion object {

    val singleCellRoom: Polyomino = mapOf(
        Vector3i.zero to Blocks.singleCellRoom
    )

  }
}

fun allPolyominoes() = enumeratePolyominoes(PolyominoeDefinitions).toSet()

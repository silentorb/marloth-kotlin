package generation.next

import generation.elements.Polyomino
import generation.elements.Side
import generation.elements.enumeratePolyominoes
import generation.elements.newBlock
import mythic.spatial.Vector3i
import org.recast4j.detour.Poly

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

class Polyominoes {
  companion object {

    val singleCellRoom: Polyomino = mapOf(
        Vector3i.zero to Blocks.singleCellRoom
    )

  }
}

class Builders {
  companion object {

    val singleCellRoom: Builder = { input ->
      listOf()
    }

  }
}

fun allPolyominoes() = enumeratePolyominoes(Polyominoes)

fun newBuilders(): Map<Polyomino, Builder> =
    mapOf(
        Polyominoes.singleCellRoom to Builders.singleCellRoom
    )

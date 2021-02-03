package generation.architecture.engine

import generation.architecture.building.emptyBuilder
import generation.architecture.matrical.BlockBuilder
import generation.general.*
import simulation.entities.Depiction
import simulation.misc.CellAttribute
import simulation.misc.absoluteCellPosition
import simulation.misc.cellHalfLength

fun blockBuildersFromElements(name: String, polyomino: Polyomino): List<BlockBuilder> {
  val elements = polyomino.elements
  val cells = polyomino.cells
  val attributes = polyomino.attributes
      .associate { it.cell to it.attributes }

  val elementOffset = -absoluteCellPosition(cells.first()) - cellHalfLength
  val mainBuilder: Builder = { input ->
    elements.map { element ->
      val config = input.general.config
      newArchitectureMesh(
          meshes = config.meshes,
          depiction = Depiction(mesh = element.target),
          position = element.location + elementOffset,
          orientation = element.orientation,
          scale = element.scale
      )
    }
  }

  val blocks = cells.mapIndexed { index, cell ->
    val cellName = "$name-${cell}"
    Block(
        name = if (index == 0) name else cellName,
        sidesOld = directionVectors.mapValues { (_, offset) ->
          val otherCell = cell + offset
          if (cells.contains(otherCell))
            Side(cellName, setOf("$name-${otherCell}"), connectionLogic = ConnectionLogic.required, isTraversable = true)
          else
            endpoint
        },
        attributes = setOf(CellAttribute.unique, CellAttribute.isTraversable) + attributes.getOrDefault(cell, listOf())
    )
  }

  return listOf(
      blocks.first() to mainBuilder
  )
      .plus(
          blocks.drop(1).map { it to emptyBuilder }
      )
}

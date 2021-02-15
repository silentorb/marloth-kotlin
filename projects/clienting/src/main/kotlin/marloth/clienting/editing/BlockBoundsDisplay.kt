package marloth.clienting.editing

import generation.architecture.engine.gatherSides
import generation.general.CellDirection
import generation.general.Side
import generation.general.horizontalDirectionVectors
import generation.general.rotateDirection
import imgui.ImColor
import marloth.definition.misc.sideGroups
import silentorb.mythic.editing.*
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.filterByProperty
import silentorb.mythic.ent.scenery.hasAttribute
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.misc.GameAttributes
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import simulation.misc.getCellPoint

val blockBoundsEnabledKey = "blockBounds"

object MarlothEditorCommands {
  const val fillOccupiedCells = "fillOccupiedCells"
  const val toggleBlockBounds = "toggleBlockBounds"
}

val cellLines = listOf(
    Vector3(-1f, -1f, -1f) to Vector3(1f, -1f, -1f),
    Vector3(-1f, -1f, -1f) to Vector3(-1f, 1f, -1f),
    Vector3(-1f, -1f, -1f) to Vector3(-1f, -1f, 1f),
    Vector3(1f, 1f, 1f) to Vector3(-1f, 1f, 1f),
    Vector3(1f, 1f, 1f) to Vector3(1f, -1f, 1f),
    Vector3(1f, 1f, 1f) to Vector3(1f, 1f, -1f),
    Vector3(1f, -1f, -1f) to Vector3(1f, -1f, 1f),
    Vector3(1f, -1f, -1f) to Vector3(1f, 1f, -1f),
    Vector3(-1f, 1f, -1f) to Vector3(-1f, 1f, 1f),
    Vector3(-1f, 1f, -1f) to Vector3(1f, 1f, -1f),
    Vector3(-1f, -1f, 1f) to Vector3(-1f, 1f, 1f),
    Vector3(-1f, -1f, 1f) to Vector3(1f, -1f, 1f),
).map { it.first * cellHalfLength to it.second * cellHalfLength }

fun getSideHeightLines(sides: List<Pair<CellDirection, Side>>) =
    sides
        .mapNotNull { (cellDirection, side) ->
          val height = side!!.height
          val dir = horizontalDirectionVectors[cellDirection.direction]?.toVector3()
          if (dir == null)
            null
          else {
            val heightOffset = Vector3(0f, 0f, height.toFloat() * cellLength / 100f - cellHalfLength)
            val rightAngleDirection = horizontalDirectionVectors[rotateDirection(1)(cellDirection.direction)]!!.toVector3()
            val middle = getCellPoint(cellDirection.cell) + dir * cellHalfLength + heightOffset
            val hookOffset = dir * 0.25f
            val a = middle + rightAngleDirection * cellHalfLength
            val b = middle - rightAngleDirection * cellHalfLength
            val ac = a + hookOffset
            val ad = a - hookOffset
            val bc = b + hookOffset
            val bd = b - hookOffset
            listOf(
                a to b,
                ac to ad,
                bc to bd,
            )
          }
        }
        .flatten()

fun getCellLines(cells: List<Vector3i>) =
    cells
        .flatMap { cell ->
          val offset = getCellPoint(cell)
          cellLines.map { line -> line.first + offset to line.second + offset }
        }
        .distinct()

fun drawBlockBounds(environment: GizmoEnvironment, graph: Graph) {
  val editor = environment.editor
  val meshNodes = filterByProperty(graph, SceneProperties.mesh).map { it.source }
  val cells = getCellOccupancy(editor.enumerations.meshShapes, graph, meshNodes)
      .distinct()

  val sideNodes = nodeAttributes(graph, GameAttributes.blockSide)
  val sides = gatherSides(sideGroups, graph, sideNodes)
      .filter { it.second != null } as List<Pair<CellDirection, Side>>

  val heightLines = getSideHeightLines(sides)
  val (primaryCells, secondaryCells) = cells.partition { cell ->
    sides.any { it.first.cell == cell }
  }
  val primaryLines = getCellLines(primaryCells)
  val secondaryLines = getCellLines(secondaryCells - primaryCells)

  val transform = environment.transform
  val drawList = environment.drawList

  val mediumColor = ImColor.intToColor(128, 128, 128, 160)
  val lightColor = ImColor.intToColor(128, 128, 128, 96)

  for ((start, end) in primaryLines) {
    drawGizmoLine(drawList, transform, start, end, mediumColor)
  }

  for ((start, end) in secondaryLines) {
    drawGizmoLine(drawList, transform, start, end, lightColor)
  }

  for ((start, end) in heightLines) {
    drawGizmoLine(drawList, transform, start, end, ImColor.intToColor(255, 255, 255, 128))
  }
}

val blockBoundsPainter = gizmoPainterToggle(blockBoundsEnabledKey) { environment ->
  val graph = getCachedGraph(environment.editor)
  if (hasAttribute(graph, GameAttributes.blockSide)) {
    drawBlockBounds(environment, graph)
  }
}

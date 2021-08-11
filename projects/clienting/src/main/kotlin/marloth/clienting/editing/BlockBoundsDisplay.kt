package marloth.clienting.editing

import generation.architecture.building.directionRotation
import generation.architecture.engine.gatherSides
import generation.architecture.engine.getCellDirection
import generation.general.*
import imgui.ImColor
import imgui.ImDrawList
import marloth.definition.misc.getSideNodes
import marloth.definition.misc.isBlockSide
import marloth.definition.misc.nonTraversableBlockSides
import marloth.definition.misc.sideGroups
import silentorb.mythic.editing.main.*
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.filterByProperty
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.scenery.anyNodeHasAttribute
import silentorb.mythic.ent.scenery.getNodesWithAttribute
import silentorb.mythic.ent.scenery.nodeHasAttribute
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.misc.*

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

val eastSidePoints = listOf(
    Vector3(1f, -1f, -1f),
    Vector3(1f, -1f, 1f),
    Vector3(1f, 1f, 1f),
    Vector3(1f, 1f, -1f),
)

data class LineSegment(
    val start: Vector3,
    val end: Vector3,
    val color: Int,
)

object LineColors {
  val height = ImColor.intToColor(255, 255, 255, 128)
  val selectedHeight = ImColor.intToColor(255, 255, 255, 200)
}

fun getSideHeightLines(selection: Collection<CellDirection>, sides: List<Pair<CellDirection, Side>>): List<LineSegment> =
    sides
        .mapNotNull { (cellDirection, side) ->
          val height = side.height
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
            val color = if (selection.contains(cellDirection))
              LineColors.selectedHeight
            else
              LineColors.height

            listOf(
                LineSegment(a, b, color),
                LineSegment(ac, ad, color),
                LineSegment(bc, bd, color),
            )
          }
        }
        .flatten()

fun getCellLines(cells: Collection<Vector3i>, color: Int): List<LineSegment> =
    cells
        .flatMap { cell ->
          val offset = getCellPoint(cell)
          cellLines.map { line ->
            LineSegment(line.first + offset, line.second + offset, color)
          }
        }
        .distinct()

private val mediumColor = ImColor.intToColor(128, 128, 128, 160)
private val lightColor = ImColor.intToColor(128, 128, 128, 96)

fun drawlines(drawList: ImDrawList, transform: ScreenTransform, lines: Collection<LineSegment>) {
  for ((start, end, color) in lines) {
    drawGizmoLine(drawList, transform, start, end, color)
  }
}

fun drawBlockBounds(environment: GizmoEnvironment, graph: Graph) {
  val editor = environment.editor
  val selection = getNodeSelection(editor)
  val meshNodes = filterByProperty(graph, SceneProperties.mesh).map { it.source }
  val cells = getCellOccupancy(editor.enumerations.resourceInfo.meshShapes, graph, meshNodes)
      .distinct()

  val sideNodes = getSideNodes(graph)
  val sides = gatherSides(sideGroups, graph, sideNodes, nonTraversableBlockSides)
      .filter { it.second != null } as List<Pair<CellDirection, Side>>

  val variableSides = selection.mapNotNull { node ->
    if (nodeHasAttribute(graph, node, GameAttributes.showIfSideIsEmpty))
      getNodeValue<CellDirection>(graph, node, GameProperties.direction)
    else
      getNodeValue<CellDirection>(graph, node, GameProperties.showIfSideIsEmpty)
  }

  val selectedSides = selection.mapNotNull { node ->
    getCellDirection(graph, node)
  }

  val heightLines = getSideHeightLines(selectedSides, sides)
  val (primaryCells, secondaryCells) = cells.partition { cell ->
    sides.any { it.first.cell == cell }
  }

  val primaryLines = getCellLines(primaryCells, mediumColor)
  val secondaryLines = getCellLines(secondaryCells - primaryCells, lightColor)
  val selectedLines = getCellLines(selectedSides.map { it.cell }, ImColor.intToColor(255, 255, 255, 200))

  val transform = environment.transform
  val drawList = environment.drawList

  for (side in variableSides) {
    val sideTransform = Matrix.identity.rotateZ(directionRotation(side.direction))
    val center = getCellPoint(side.cell)
    val points = eastSidePoints.map {
      center + it.transform(sideTransform) * cellHalfLength
    }
    drawGizmoSolidPolygon(drawList, transform, points, ImColor.intToColor(0, 128, 128, 64))
  }

  val lines = primaryLines + secondaryLines + heightLines + selectedLines
  drawlines(drawList, transform, lines)
}

fun drawWorldBlockBounds(environment: GizmoEnvironment, blockGrid: BlockGrid) {
  val transform = environment.transform
  val drawList = environment.drawList

  val lines = getCellLines(blockGrid.keys, lightColor)
  drawlines(drawList, transform, lines)

  for ((cell, block) in blockGrid) {
    val center = getCellPoint(cell)
    val offset = block.offset
    val text = "${cell.x}, ${cell.y}, ${cell.z}\n${block.source.name}\n${block.source.turns}\n" +
        "${offset.x}, ${offset.y}, ${offset.z}"
//        "${block.source.heightOffset}"
    drawGizmoText(drawList, transform, center, text, mediumColor)
  }
}

val blockBoundsPainter = gizmoPainterToggle(blockBoundsEnabledKey) { environment ->
  val editor = environment.editor
  val graph = getCachedGraph(editor)
  if (graph.any(::isBlockSide)) {
    drawBlockBounds(environment, graph)
  }
  val blockGrid = staticDebugBlockGrid
  if (blockGrid != null && getActiveEditorGraphKey(editor) == activeWorldKey) {
    drawWorldBlockBounds(environment, blockGrid)
  }
}

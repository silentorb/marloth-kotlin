package marloth.clienting.editing

import imgui.ImColor
import silentorb.mythic.editing.drawGizmoLine
import silentorb.mythic.editing.getCachedGraph
import silentorb.mythic.editing.gizmoPainterToggle
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.filterByProperty
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.getShape
import silentorb.mythic.ent.scenery.hasAttribute
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.misc.GameAttributes
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import simulation.misc.getCellPoint
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

val blockBoundsEnabledKey = "blockBounds"

object MarlothEditorCommands {
  const val toggleBlockBounds = "toggleBlockBounds"
}

fun locationAxisToCellAxis(value: Float): Float =
    value / cellLength

fun getOverlappingCells(meshShapes: Map<Key, Shape>, graph: Graph, nodes: Collection<Key>): List<Vector3i> =
    nodes.flatMap { node ->
      val transform = getNodeTransform(graph, node)
      val shape = getShape(meshShapes, graph, node)
      if (shape == null)
        listOf()
      else {
        val location = transform.translation()
        val radius = shape.radius * transform.getScale().x
        val minX = ceil(locationAxisToCellAxis(location.x - radius)).toInt()
        val minY = ceil(locationAxisToCellAxis(location.y - radius)).toInt()
        val minZ = ceil(locationAxisToCellAxis(location.z - radius)).toInt()
        val maxX = floor(locationAxisToCellAxis(location.x + radius)).toInt()
        val maxY = floor(locationAxisToCellAxis(location.y + radius)).toInt()
        val maxZ = floor(locationAxisToCellAxis(location.z + radius)).toInt()
        (minZ..maxZ).flatMap { z ->
          (minY..maxY).flatMap { y ->
            (minX..maxX).map { x ->
              Vector3i(x, y, z)
            }
          }
        }
      }
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

val blockBoundsPainter = gizmoPainterToggle(blockBoundsEnabledKey) { environment ->
  val editor = environment.editor
  val graph = getCachedGraph(editor)
  if (hasAttribute(graph, GameAttributes.blockSide)) {
    val meshNodes = filterByProperty(graph, SceneProperties.mesh).map { it.source }
    val cells = getOverlappingCells(editor.enumerations.meshShapes, graph, meshNodes)
        .distinct()
//    val cells = listOf(Vector3i(0, 0, 0))

    val lines = cells
        .flatMap { cell ->
          val offset = getCellPoint(cell)
          cellLines.map { line -> line.first + offset to line.second + offset }
        }
        .distinct()

    val transform = environment.transform
    val drawList = environment.drawList

    for ((start, end) in lines) {
      drawGizmoLine(drawList, transform, start, end, ImColor.intToColor(128, 128, 128, 255))
    }
  }
}

package marloth.clienting.editing

import generation.architecture.engine.gatherSides
import generation.general.horizontalDirectionVectors
import generation.general.rotateDirection
import imgui.ImColor
import marloth.definition.misc.sideGroups
import silentorb.mythic.editing.drawGizmoLine
import silentorb.mythic.editing.getCachedGraph
import silentorb.mythic.editing.gizmoPainterToggle
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.filterByProperty
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.getShape
import silentorb.mythic.ent.scenery.hasAttribute
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.scenery.Box
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.ShapeTransform
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import simulation.misc.GameAttributes
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import simulation.misc.getCellPoint
import kotlin.math.ceil
import kotlin.math.floor

val blockBoundsEnabledKey = "blockBounds"

object MarlothEditorCommands {
  const val toggleBlockBounds = "toggleBlockBounds"
}

fun locationAxisToCellAxis(value: Float): Float =
    value / cellLength

val boxPoints = (-1..1 step 2).flatMap { x ->
  (-1..1 step 2).flatMap { y ->
    (-1..1 step 2).map { z ->
      Vector3(x.toFloat(), y.toFloat(), z.toFloat())
    }
  }
}

fun getShapeBounds(shape: Shape, transform: Matrix): Pair<Vector3, Vector3> =
    when (shape) {
      is Box -> {
        val localTransform = transform.scale(shape.halfExtents)
        val points = boxPoints.map { point ->
          point.transform(localTransform)
        }
        val a = Vector3(points.minOf { it.x }, points.minOf { it.y }, points.minOf { it.z })
        val b = Vector3(points.maxOf { it.x }, points.maxOf { it.y }, points.maxOf { it.z })
        a to b
      }
      is ShapeTransform -> {
        getShapeBounds(shape, transform * shape.transform)
      }
      else -> {
        val location = transform.translation()
        val radius = shape.radius * transform.getScale().x
        (location - radius) to (location + radius)
      }
    }

fun getOverlappingCells(meshShapes: Map<Key, Shape>, graph: Graph, nodes: Collection<Key>): List<Vector3i> =
    nodes.flatMap { node ->
      val transform = getNodeTransform(graph, node)
      val shape = getShape(meshShapes, graph, node)
      if (shape == null)
        listOf()
      else {
        val (min, max) = getShapeBounds(shape, transform)
        val minX = ceil(locationAxisToCellAxis(min.x)).toInt()
        val minY = ceil(locationAxisToCellAxis(min.y)).toInt()
        val minZ = ceil(locationAxisToCellAxis(min.z)).toInt()
        val maxX = floor(locationAxisToCellAxis(max.x)).toInt()
        val maxY = floor(locationAxisToCellAxis(max.y)).toInt()
        val maxZ = floor(locationAxisToCellAxis(max.z)).toInt()
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

    val sideNodes = nodeAttributes(graph, GameAttributes.blockSide)
    val sides = gatherSides(sideGroups, graph, sideNodes)
        .filter { it.second != null }

    val heightLines = sides
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

    val lines = cells
        .flatMap { cell ->
          val offset = getCellPoint(cell)
          cellLines.map { line -> line.first + offset to line.second + offset }
        }
        .distinct()

    val transform = environment.transform
    val drawList = environment.drawList

    for ((start, end) in lines) {
      drawGizmoLine(drawList, transform, start, end, ImColor.intToColor(128, 128, 128, 128))
    }

    for ((start, end) in heightLines) {
      drawGizmoLine(drawList, transform, start, end, ImColor.intToColor(255, 255, 255, 128))
    }
  }
}

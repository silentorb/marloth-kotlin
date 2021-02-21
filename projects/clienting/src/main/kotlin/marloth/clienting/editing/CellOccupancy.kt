package marloth.clienting.editing

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.getShape
import silentorb.mythic.scenery.Box
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.ShapeTransform
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.misc.cellLength
import kotlin.math.ceil
import kotlin.math.floor

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
        getShapeBounds(shape.shape, transform * shape.transform)
      }
      else -> {
        val location = transform.translation()
        val radius = shape.radius * transform.getScale().x
        (location - radius) to (location + radius)
      }
    }

fun lesserToGreater(a: Int, b: Int) =
    if (a > b)
      b to a
    else
      a to b


fun getCellOccupancy(meshShapes: Map<Key, Shape>, graph: Graph, nodes: Collection<Key>): List<Vector3i> =
    nodes.flatMap { node ->
      val transform = getNodeTransform(graph, node)
      val shape = getShape(meshShapes, graph, node)
      if (shape == null)
        listOf()
      else {
        val (min, max) = getShapeBounds(shape, transform)
        val (minX, maxX) = lesserToGreater(
            ceil(locationAxisToCellAxis(min.x)).toInt(),
            floor(locationAxisToCellAxis(max.x)).toInt()
        )
        val (minY, maxY) = lesserToGreater(
            ceil(locationAxisToCellAxis(min.y)).toInt(),
            floor(locationAxisToCellAxis(max.y)).toInt()
        )
        val (minZ, maxZ) = lesserToGreater(
            ceil(locationAxisToCellAxis(min.z)).toInt(),
            floor(locationAxisToCellAxis(max.z)).toInt()
        )
        (minZ..maxZ).flatMap { z ->
          (minY..maxY).flatMap { y ->
            (minX..maxX).map { x ->
              Vector3i(x, y, z)
            }
          }
        }
      }
    }

package lab.views.world

import simulation.WorldBoundary
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.TextStyle
import org.joml.plus
import org.joml.xy
import rendering.Renderer
import simulation.*

private val gridSpacing = 10f // In world units

fun getScale(bounds: Bounds, worldDimensions: Vector3): Float {
  val padding = worldBorderPadding * 2
  if (worldDimensions.x != worldDimensions.y)
    throw Error("getScale currently only supports worlds with identical x and y dimensions.")

  if (bounds.dimensions.x > bounds.dimensions.y) {
    return (bounds.dimensions.y - padding) / worldDimensions.y
  } else {
    return (bounds.dimensions.x - padding) / worldDimensions.x
  }
}

fun drawGrid(canvas: Canvas, bounds: Bounds, worldBoundary: WorldBoundary, scale: Float) {
  val offset = bounds.position + worldBorderPadding
  val steps = (worldBoundary.dimensions.x / gridSpacing).toInt() - 1
  val gridColor = Vector4(0f, 0f, 0f, 0.3f)
  val gridGap = gridSpacing * scale
  val gridBrush = canvas.solid(gridColor)
  val length = worldBoundary.dimensions.x * scale
  for (i in 0..steps) {
    canvas.drawSquare(
        Vector2(0f, i * gridGap) + offset,
        Vector2(length, 1f),
        gridBrush
    )

    canvas.drawSquare(
        Vector2(i * gridGap, 0f) + offset,
        Vector2(1f, length),
        gridBrush
    )
  }
}

fun drawAbstractWorld(bounds: Bounds, getPosition: PositionFunction, canvas: Canvas, world: AbstractWorld,
                      renderer: Renderer) {
  val solid = canvas.solid(Vector4(0.7f, 0.6f, 0.5f, 0.6f))
  val spaceSolid = canvas.solid(Vector4(0.7f, 0.6f, 0.5f, 0.3f))
  val outline = canvas.outline(Vector4(0.3f, 0f, 0f, 0.8f), 3f)
  val scale = getScale(bounds, world.boundary.dimensions)
//  val offset = bounds.position + worldPadding

//  fun getPosition(position: Vector2) = offset + (Vector2(position.x, -position.y)
//      - world.boundary.start.xy) * scale

  fun getNodePosition(node: Node) = getPosition(node.position.xy)

//  fun getPosition(node: IndexedNode) = offset + (Vector2(node.position.x, -node.position.y)
//      - world.boundary.start.xy) * scale

  val style = TextStyle(
      canvas.fonts[0],
      12f,
      Vector4(1f, 1f, 1f, 1f)
  )

  for (node in world.nodes) {
    val radius = node.radius * scale
    val position = getNodePosition(node)
    val circleBrush = if (node.isWalkable) solid else spaceSolid
    canvas.drawSolidCircle(position, radius, circleBrush)
    canvas.drawCircle(position, radius, outline)
    canvas.drawText(node.index.toString() + " " + node.walls.size,
        position,
        style)
  }

  for (connection in world.connections) {
    val color = when (connection.type) {
      ConnectionType.union -> Vector4(0.1f, 0f, 0f, 0.4f)
      ConnectionType.obstacle -> Vector4(0f, 0.5f, 0f, 0.3f)
      else -> Vector4(0f, 0.5f, 0f, 0.8f)
    }

    canvas.drawLine(getNodePosition(connection.first), getNodePosition(connection.second), color, 3f)
  }

  for (node in world.nodes) {
    if (node.floors.any()) {
      val position = getNodePosition(node)
      canvas.drawText(node.index.toString() + " " + node.floors.first().unorderedVertices.size.toString(),
          position,
          style)
    }
  }

//  canvas.drawSolidCircle(getPosition(Vector2(-32.670635f,23.672432f)), 2f,
//      canvas.solid(Vector4(1f, 0.6f, 0.5f, 1f)))
}

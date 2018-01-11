package lab

import generation.AbstractWorld
import generation.ConnectionType
import generation.Node
import generation.WorldBoundary
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.minus
import org.joml.plus
import org.joml.xy

private val worldPadding = 20f // In screen units
private val gridSpacing = 10f // In world units

fun getScale(bounds: Bounds, worldBoundary: WorldBoundary): Float {
  val worldDimensions = worldBoundary.dimensions
  val padding = worldPadding * 2
  if (worldDimensions.x != worldDimensions.y)
    throw Error("getScale currently only supports worlds with identical x and y dimensions.")

  if (bounds.dimensions.x > bounds.dimensions.y) {
    return (bounds.dimensions.y - padding) / worldDimensions.y
  } else {
    return (bounds.dimensions.x - padding) / worldDimensions.x
  }
}

fun drawGrid(canvas: Canvas, bounds: Bounds, worldBoundary: WorldBoundary, scale: Float) {
  val offset = bounds.position + worldPadding
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

fun drawAbstractWorld(bounds: Bounds, canvas: Canvas, world: AbstractWorld) {
  val solid = canvas.solid(Vector4(0.7f, 0.6f, 0.5f, 0.6f))
  val outline = canvas.outline(Vector4(0.3f, 0f, 0f, 0.8f), 3f)
  val scale = getScale(bounds, world.boundary)
  val offset = bounds.position + worldPadding

  fun getPosition(position: Vector2) = offset + (Vector2(position.x, - position.y)
      - world.boundary.start.xy) * scale

  fun getPosition(node: Node) = offset + (Vector2(node.position.x, - node.position.y)
      - world.boundary.start.xy) * scale

  drawGrid(canvas, bounds, world.boundary, scale)

  for (node in world.nodes) {
    val radius = node.radius * scale
    val position = getPosition(node)
    canvas.drawSolidCircle(position, radius, solid)
    canvas.drawCircle(position, radius, outline)
  }

  for (connection in world.connections) {
    val color = if (connection.type == ConnectionType.union)
      Vector4(0.1f, 0f, 0f, 0.4f)
    else
      Vector4(0f, 0.5f, 0f, 0.8f)

    canvas.drawLine(getPosition(connection.first), getPosition(connection.second), color, 3f)
  }

  canvas.drawSquare(
      offset,
      world.boundary.dimensions.xy * scale,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )

  canvas.drawSolidCircle(getPosition(Vector2(-32.670635f,23.672432f)), 2f,
      canvas.solid(Vector4(1f, 0.6f, 0.5f, 1f)))
}

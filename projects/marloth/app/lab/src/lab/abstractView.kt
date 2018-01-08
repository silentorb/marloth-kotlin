package lab

import generation.AbstractWorld
import generation.Node
import generation.WorldBoundary
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.minus
import org.joml.plus
import org.joml.xy

private val worldPadding = 20f

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

fun drawAbstractWorld(bounds: Bounds, canvas: Canvas, world: AbstractWorld) {
  val solid = canvas.solid(Vector4(1f, 1f, 0f, 1f))
  val outline = canvas.outline(Vector4(1f, 0f, 0f, 1f), 5f)
  val scale = getScale(bounds, world.boundary)
  val offset = bounds.position + worldPadding
  fun getPosition(node: Node) = offset + (node.position.xy - world.boundary.start.xy) * scale

  for (node in world.nodes) {
    val radius = node.radius * scale
    val position = getPosition(node)
    canvas.drawSolidCircle(position, radius, solid)
    canvas.drawCircle(position, radius, outline)
  }

  for (connection in world.connections) {
    canvas.drawLine(getPosition(connection.first), getPosition(connection.second), Vector4(0f, 0.6f, 0f, 1f), 5f)
  }

  canvas.drawSquare(
      bounds.position + worldPadding,
      bounds.dimensions - worldPadding * 2,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )
}

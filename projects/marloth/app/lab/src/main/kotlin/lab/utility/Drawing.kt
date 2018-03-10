package lab.utility

import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector4

fun drawFill(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.solid(color))
}

fun drawBorder(bounds: Bounds, canvas: Canvas, color: Vector4) {
  canvas.drawSquare(bounds.position, bounds.dimensions, canvas.outline(color, 1f))
}

fun grayTone(value: Float) = Vector4(value, value, value, 1f)

fun grayTone(value: Float, alpha: Float) = Vector4(value, value, value, alpha)
package lab.views

import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import rendering.createCheckers

fun drawTextureView(bounds: Bounds, canvas: Canvas) {
  val texture = createCheckers()

  val length = Math.min(bounds.dimensions.x, bounds.dimensions.y)
  canvas.drawImage(bounds.position, Vector2(length, length), canvas.image(texture))
  texture.dispose()
}
package lab.views

import marloth.texture_generation.checkerPattern
import marloth.texture_generation.createTexture
import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector3

fun drawTextureView(bounds: Bounds, canvas: Canvas) {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  val texture = createTexture(checkerPattern(black, white), 256)

  val length = Math.min(bounds.dimensions.x, bounds.dimensions.y)
  canvas.drawImage(bounds.position, Vector2(length, length), canvas.image(texture))
  texture.dispose()
}
package lab.views

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import org.joml.Vector2i
import rendering.Renderer
import rendering.Textures
import rendering.textureLibrary
import texture_generation.createTexture

data class TextureViewConfig(
    var texture: Textures = Textures.checkers
)

fun drawTextureView(renderer: Renderer, config: TextureViewConfig, bounds: Bounds, canvas: Canvas) {
  val texture = createTexture(textureLibrary[config.texture]!!(), 256)
  val length = Math.min(bounds.dimensions.x, bounds.dimensions.y)
  val repeat = 2f
  canvas.drawDynamicImage(bounds.position, Vector2(length, length), canvas.image(texture), listOf(
      0f, 1f, 0f, repeat,
      0f, 0f, 0f, 0f,
      1f, 0f, repeat, 0f,
      1f, 1f, repeat, repeat
  ))
  texture.dispose()
}

class TextureView {

  fun createLayout(renderer: Renderer, config: TextureViewConfig, dimensions: Vector2i): List<Box> {
    val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }

    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), draw),
        Pair(Measurement(Measurements.stretch, 0f), { b: Bounds, c: Canvas ->
          drawTextureView(renderer, config, b, c)
          draw(b, c)
        })
    )
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    val boxes = arrangeMeasuredList(measuredHorizontalArrangement, panels, dimensions2)

    return boxes
  }
}
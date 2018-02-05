package lab.views

import lab.LabConfig
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import rendering.createCheckers

fun drawTextureView(bounds: Bounds, canvas: Canvas) {
  val texture = createCheckers()

  val length = Math.min(bounds.dimensions.x, bounds.dimensions.y)
  canvas.drawImage(bounds.position, Vector2(length, length), canvas.image(texture))
  texture.dispose()
}

class TextureView : View {

  override fun createLayout(dimensions: Vector2): LabLayout {
    val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }

    val panels = listOf(
        Pair(Measurement(Measurements.pixel, 200f), draw),
        Pair(Measurement(Measurements.stretch, 0f), { b: Bounds, c: Canvas ->
          drawTextureView(b, c)
          draw(b, c)
        })
    )
    val boxes = overlap(createVerticalBounds(panels.map { it.first }, dimensions), panels, { a, b ->
      Box(a, b.second)
    })

    return LabLayout(
        boxes
    )
  }

  override fun input(): ViewInputResult {

  }
}
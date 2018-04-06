package marloth.clienting.gui

import mythic.bloom.Bounds
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import org.joml.plus
import rendering.SceneRenderer

fun renderGui(renderer: SceneRenderer, bounds: Bounds, canvas: Canvas) {
  canvas.drawText(TextConfiguration("Testing",
      renderer.renderer.fonts[0], 12f, bounds.position + Vector2(10f, 10f), Vector4(1f)))
}
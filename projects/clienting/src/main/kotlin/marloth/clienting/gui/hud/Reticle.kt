package marloth.clienting.gui.hud

import silentorb.mythic.bloom.*
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2

fun reticleDepiction(radius: Float, color: Vector4): Depiction = { bounds, canvas ->
  val position = bounds.position.toVector2()
  val thickness = 2f
  val x = Vector2(radius, 0f)
  val y = Vector2(0f, radius)
  enableBloomBlending()
  canvas.drawLine(position - x, position + x, color, thickness)
  canvas.drawLine(position - y, position + y, color, thickness)
}

fun reticlePlacement(): Flower {
  return centered(
      Box(
          dimensions = Vector2i(20),
          depiction = reticleDepiction(15f, Vector4(1f, 1f, 1f, 0.1f))
      )
  )
}

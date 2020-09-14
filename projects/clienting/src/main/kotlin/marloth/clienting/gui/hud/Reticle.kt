package marloth.clienting.gui.hud

import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2

fun reticleDepiction(radius: Float, color: Vector4): Depiction = { bounds, canvas ->
  val position = bounds.position.toVector2()
  val thickness = 2f
  val x = Vector2(radius, 0f)
  val y = Vector2(0f, radius)
  canvas.drawLine(position - x, position + x, color, thickness)
  canvas.drawLine(position - y, position + y, color, thickness)
}

fun reticlePlacement(): Flower {
  return depict(reticleDepiction(15f, Vector4(1f, 1f, 1f, 0.4f)))
//  return div("b",
//        reverse = reverseOffset(left = centered, top = centered))(
//      div(forward = forwardDimensions(fixed(30), fixed(30)))(
//          depict(reticleDepiction(15f, Vector4(1f, 1f, 1f, 0.4f)))
//      )
//  )
}

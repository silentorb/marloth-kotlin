package rendering.drawing

import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import rendering.Renderer
import scenery.enums.MeshId

fun renderSkySphere(renderer: Renderer){
  globalState.depthEnabled = false
  val mesh = renderer.meshes[MeshId.skySphere.name]!!
  val primitive = mesh.primitives[0]
  drawPrimitive(renderer, primitive, Matrix().scale(100f), color =
  Vector4(1f, 1f, 1f, 0.5f))
  globalState.depthEnabled = true
}

package rendering.drawing

import mythic.glowing.globalState
import mythic.spatial.Matrix
import rendering.Renderer
import scenery.enums.MeshId

fun renderSkySphere(renderer: Renderer){
  globalState.depthEnabled = false
  val mesh = renderer.meshes[MeshId.skySphere.name]!!
  val primitive = mesh.primitives[0]
  drawPrimitive(renderer, primitive, Matrix().scale(100f))
  globalState.depthEnabled = true
}

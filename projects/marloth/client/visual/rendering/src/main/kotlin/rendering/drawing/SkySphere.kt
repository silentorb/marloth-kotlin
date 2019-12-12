package rendering.drawing

import mythic.glowing.globalState
import rendering.ElementGroups
import rendering.Renderer
import scenery.Camera

fun renderBackground(renderer: Renderer, camera: Camera, background: ElementGroups) {
  globalState.depthEnabled = false
  for (group in background) {
    renderElementGroup(renderer, camera, group)
  }
//  val mesh = renderer.meshes[MeshId.skySphere.name]!!
//  val primitive = mesh.primitives[0]
//  drawPrimitive(renderer, primitive, Matrix().scale(100f), color = Vector4(1f, 1f, 1f, 0.5f))
  globalState.depthEnabled = true
}

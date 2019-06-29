package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import org.lwjgl.opengl.GL11
import rendering.shading.ObjectShaderConfig
import scenery.MeshId

fun renderBillboard(gameRenderer: GameSceneRenderer, billboard: TexturedBillboard) {
  val mesh = gameRenderer.renderer.meshes[MeshId.billboard]!!
  val camera = gameRenderer.scene.camera
  val effects = gameRenderer.renderer.effects
  val transform = Matrix()
      .billboardCylindrical(billboard.position, camera.position, Vector3(0f, 0f, 1f))
      .scale(billboard.scale / 2f)

  val textures = gameRenderer.renderer.renderer.textures
  val texture = textures[billboard.texture.toString()]
  if (texture == null)
    return

  globalState.blendEnabled = true
//  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
  effects.billboard.activate(ObjectShaderConfig(
      texture = texture,
      transform = transform,
      color = billboard.color
  ))

  mesh.primitives.first().mesh.draw(DrawMethod.triangleFan)
  globalState.blendEnabled = false
}

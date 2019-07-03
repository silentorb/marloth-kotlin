package rendering

import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import rendering.shading.ObjectShaderConfig
import rendering.shading.createInstanceBuffer
import scenery.MeshId

fun renderBillboard(gameRenderer: GameSceneRenderer, billboards: List<TexturedBillboard>) {
  val model = gameRenderer.renderer.meshes[MeshId.billboard]!!
  val camera = gameRenderer.scene.camera
  val effects = gameRenderer.renderer.effects
  val transforms = billboards.map { billboard ->
    Matrix()
        .billboardCylindrical(billboard.position, camera.position, Vector3(0f, 0f, 1f))
        .scale(billboard.scale / 2f)
  }

  val textures = gameRenderer.renderer.renderer.textures
  val texture = textures[billboards.first().texture.toString()]
  if (texture == null)
    return

  globalState.blendEnabled = true
//  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
  debugMarkPass(true, "Particles") {

    effects.billboard.activate(ObjectShaderConfig(
        texture = texture,
        color = billboards.first().color
    ))

    gameRenderer.renderer.renderer.instanceBuffer.load(createInstanceBuffer(transforms))
    val mesh = model.primitives.first().mesh
    drawMeshInstanced(mesh, DrawMethod.triangleFan, billboards.size)
    globalState.blendEnabled = false
  }
}

package rendering.drawing

import mythic.glowing.*
import mythic.spatial.*
import rendering.SceneRenderer
import rendering.TexturedBillboard
import rendering.shading.ObjectShaderConfig
import rendering.shading.ShaderFeatureConfig
import rendering.shading.createInstanceBuffer
import rendering.shading.padBuffer
import kotlin.math.roundToInt

fun renderBillboard(sceneRenderer: SceneRenderer, billboards: List<TexturedBillboard>) {
  val renderer = sceneRenderer.renderer
  val model = sceneRenderer.meshes["billboard"]!!
  val camera = sceneRenderer.camera
  val textures = renderer.textures
  val texture = textures[billboards.first().texture.toString()]
  if (texture == null)
    return

  debugMarkPass(true, "Particles") {
    globalState.blendEnabled = true
    val isTextureAnimated = texture.width != texture.height
    val textureScale = if (isTextureAnimated)
      Vector2(texture.height.toFloat() / texture.width.toFloat(), 1f)
    else
      null

    val steps = (texture.width.toFloat() / texture.height.toFloat()).roundToInt().toFloat()

    val mesh = model.primitives.first().mesh
    val shader = renderer.getShader(mesh.vertexSchema, ShaderFeatureConfig(
        texture = true,
        instanced = true,
        animatedTexture = true
    ))
    shader.activate(ObjectShaderConfig(
        texture = texture,
        color = billboards.first().color,
        textureScale = textureScale
    ))

    renderer.uniformBuffers.instance.load(createInstanceBuffer { buffer ->
      for (billboard in billboards) {
        val transform = Matrix()
            .billboardCylindrical(billboard.position, camera.position, Vector3(0f, 0f, 1f))
            .scale(billboard.scale)
        buffer.putMatrix(transform)
        buffer.putVector4(billboard.color)
        buffer.putFloat(billboard.step.toFloat() / steps)
        buffer.putFloat(0f)
        padBuffer(buffer, 2)
      }
    })
    drawMeshInstanced(mesh, DrawMethod.triangleFan, billboards.size)
    globalState.blendEnabled = false
  }
}

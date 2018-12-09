package rendering

import mythic.drawing.getStaticCanvasDependencies
import mythic.glowing.DrawMethod
import mythic.glowing.activateTextures
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.joml.Vector2i
import org.joml.Vector4i
import rendering.meshes.ModelMap
import rendering.shading.ObjectShaderConfig
import rendering.shading.ScreenShader
import rendering.shading.Shaders
import scenery.Textures

fun renderSkyBox(textures: TextureLibrary, meshes: ModelMap, shaders: Shaders) {
  /*
  val texture = textures[Textures.background]!!
  val mesh = meshes[MeshType.skybox]!!
  shaders.texturedFlat.activate(ObjectShaderConfig(
      texture = texture,
      transform = Matrix().scale(900f)
  ))
  mesh.primitives[0].mesh.draw(DrawMethod.triangleFan)
  */
}

typealias ScreenFilter = (Shaders) -> Unit

fun getDisplayConfigFilters(config: DisplayConfig): List<ScreenFilter> =
    if (config.depthOfField)
      listOf<ScreenFilter>(
          { it.depthOfField.activate() }
      )
    else
      listOf()

class GameSceneRenderer(
    val scene: GameScene,
    val renderer: SceneRenderer
) {

  fun prepareRender(filters: List<ScreenFilter>) {
    globalState.depthEnabled = true
    val glow = renderer.renderer.glow
    if (filters.any()) {
      val offscreenBuffer = renderer.renderer.offscreenBuffer
      val dimensions = Vector2i(offscreenBuffer.colorTexture.width, offscreenBuffer.colorTexture.height)
      glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
      glow.state.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
    }
    glow.operations.clearScreen()
  }

  fun finishRender(dimensions: Vector2i, filters: List<ScreenFilter>) {
    globalState.cullFaces = false
    globalState.setFrameBuffer(0)
    globalState.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
    for (filter in filters) {
      applyFrameBufferTexture(filter)
    }

    globalState.cullFaces = true
  }

  fun applyFrameBufferTexture(filter: ScreenFilter) {
    val canvasDependencies = getStaticCanvasDependencies()
    val offscreenBuffer = renderer.renderer.offscreenBuffer
    filter(renderer.renderer.shaders)
    activateTextures(listOf(offscreenBuffer.colorTexture, offscreenBuffer.depthTexture!!))
    canvasDependencies.meshes.image.draw(DrawMethod.triangleFan)
  }

  fun renderElements() {
    for (group in scene.elementGroups) {
      renderElementGroup(this, group)
    }
  }

  fun renderSectorMesh(sector: SectorMesh) {
    var index = 0
    for (textureId in sector.textureIndex) {
      try {
        val texture = renderer.renderer.mappedTextures[textureId] ?: renderer.renderer.textures[textureId.toString()]!!
        texture.activate()
        sector.mesh.drawElement(DrawMethod.triangleFan, index++)
      } catch (ex: KotlinNullPointerException) {
        val k = 0
      }
    }
  }

  fun renderWorldMesh() {
    globalState.cullFaces = true
    val worldMesh = renderer.renderer.worldMesh
    if (worldMesh != null) {
      val effectConfig = ObjectShaderConfig(
          transform = Matrix(),
          texture = renderer.renderer.mappedTextures[Textures.checkers]!!,
          color = Vector4(1f),
          normalTransform = Matrix()
      )
      renderer.effects.textured.activate(effectConfig)
      for (sector in worldMesh.sectors) {
        renderSectorMesh(sector)
      }
    }
  }

//  fun render() {
//    prepareRender()
//    renderWorldMesh()
//    renderElements()
//  }

}

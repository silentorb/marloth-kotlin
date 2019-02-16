package rendering

import mythic.drawing.getStaticCanvasDependencies
import mythic.glowing.DrawMethod
import mythic.glowing.activateTextures
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.times
import rendering.meshes.ModelMap
import rendering.shading.ObjectShaderConfig
import rendering.shading.Shaders
import rendering.texturing.TextureLibrary
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

fun drawSkeleton(renderer: SceneRenderer, armature: Armature, transforms: List<Matrix>, modelTransform: Matrix) {
  armature.bones
      .filter { it.parent != -1 }
      .forEach { bone ->
        val a = Vector3().transform(modelTransform * transforms[bone.index])
        val b = Vector3().transform(modelTransform * transforms[bone.parent])
        val white = Vector4(1f)
        renderer.drawLine(a, b, white, 2f)
      }
}

fun renderArmatures(renderer: GameSceneRenderer) {
    globalState.depthEnabled = false
    renderer.scene.elementGroups.filter { it.armature != null }
        .forEach { group ->
          val armature = renderer.renderer.renderer.armatures[group.armature]!!
          drawSkeleton(renderer.renderer, armature, armatureTransforms(armature, group), group.meshes.first().transform)
        }
    globalState.depthEnabled = true
}

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
      val offscreenBuffer = renderer.renderer.offscreenBuffers.first()
      val dimensions = Vector2i(offscreenBuffer.colorTexture.width, offscreenBuffer.colorTexture.height)
      glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
      glow.state.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
    }
    glow.operations.clearScreen()
  }

  fun finishRender(dimensions: Vector2i, filters: List<ScreenFilter>) {
    globalState.cullFaces = false
    globalState.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)

    for (filter in filters.dropLast(1)) {
//      globalState.setFrameBuffer(renderer.renderer.offscreenBuffers.first().framebuffer.id)
      applyFrameBufferTexture(filter)
    }

    globalState.setFrameBuffer(0)

    if (filters.any()) {
      applyFrameBufferTexture(filters.last())
    }

//    globalState.cullFaces = true
  }

  fun applyFrameBufferTexture(filter: ScreenFilter) {
    val canvasDependencies = getStaticCanvasDependencies()
    val offscreenBuffer = renderer.renderer.offscreenBuffers.first()
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

package rendering

import mythic.drawing.getStaticCanvasDependencies
import mythic.glowing.DrawMethod
import mythic.glowing.activateTextures
import mythic.glowing.applyOffscreenBuffer
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.joml.Vector2i
import org.joml.Vector4i
import org.lwjgl.opengl.GL11
import rendering.meshes.MeshMap
import scenery.DepictionType
import scenery.GameScene
import scenery.Textures
import scenery.VisualElement

fun renderSkyBox(textures: TextureLibrary, meshes: MeshMap, shaders: Shaders) {
  val texture = textures[Textures.background]!!
  val mesh = meshes[MeshType.skybox]!!
  shaders.texturedFlat.activate(ObjectShaderConfig(
      texture = texture,
      transform = Matrix().scale(900f)
  ))
  mesh.primitives[0].mesh.draw(DrawMethod.triangleFan)
}

class GameSceneRenderer(
    val scene: GameScene,
    val renderer: SceneRenderer
) {

  fun prepareRender() {
//    globalState.viewport = renderer.viewport
    globalState.depthEnabled = true
    val glow = renderer.renderer.glow
    val offscreenBuffer = renderer.renderer.offscreenBuffer
    val dimensions = Vector2i(offscreenBuffer.colorTexture.width, offscreenBuffer.colorTexture.height)
    glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
    glow.state.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
    glow.operations.clearScreen()
  }

  fun finishRender(dimensions:Vector2i) {
    val offscreenBuffer = renderer.renderer.offscreenBuffer
//    applyOffscreenBuffer(offscreenBuffer, dimensions, true)
    globalState.cullFaces = false
    globalState.setFrameBuffer(0)
    globalState.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
    applyFrameBufferTexture()
    globalState.cullFaces = true
  }

  fun applyFrameBufferTexture() {
    val canvasDependencies = getStaticCanvasDependencies()
    val offscreenBuffer = renderer.renderer.offscreenBuffer
    renderer.renderer.shaders.screen.activate()
    activateTextures(listOf(offscreenBuffer.colorTexture))
    canvasDependencies.meshes.image.draw(DrawMethod.triangleFan)
  }

  fun lookupMesh(depiction: DepictionType) = renderer.meshes[simplePainterMap[depiction]]!!

  fun renderElement(element: VisualElement) {
    val childDetails = scene.elementDetails.children[element.id]
    if (childDetails != null) {
      val mesh = lookupMesh(element.depiction)
      advancedPainter(mesh, renderer.renderer,element, renderer.effects)
//      humanPainter(renderer, mesh.primitives)(element, renderer.effects, childDetails)
    } else {
      val mesh = lookupMesh(element.depiction)
      simplePainter(mesh.primitives,element, renderer.effects)
    }
  }

  fun renderElements() {
    for (element in scene.elements) {
      renderElement(element)
    }
  }

  fun renderSectorMesh(sector: SectorMesh) {
    var index = 0
    for (textureId in sector.textureIndex) {
      val texture = renderer.renderer.textures[textureId]!!
      texture.activate()
      sector.mesh.drawElement(DrawMethod.triangleFan, index++)
    }
  }

  fun renderWorldMesh() {
    globalState.cullFaces = true
    val worldMesh = renderer.renderer.worldMesh
    if (worldMesh != null) {
      val effectConfig = ObjectShaderConfig(
          transform = Matrix(),
          texture = renderer.renderer.textures[Textures.checkers]!!,
          color = Vector4(1f),
          normalTransform = Matrix()
      )
      renderer.effects.textured.activate(effectConfig)
      for (sector in worldMesh.sectors) {
        renderSectorMesh(sector)
      }
    }
  }

  fun render() {
    prepareRender()
    renderWorldMesh()
    renderElements()
  }

}

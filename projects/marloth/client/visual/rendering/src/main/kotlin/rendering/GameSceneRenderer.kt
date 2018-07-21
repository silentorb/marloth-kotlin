package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.globalState
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL11
import rendering.meshes.MeshMap
import scenery.DepictionType
import scenery.GameScene
import scenery.Textures
import scenery.VisualElement

data class Temp(
    val foo: Int
)

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
    globalState.viewport = renderer.viewport
    globalState.depthEnabled = true
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
    val r = renderer.renderer

    renderSkyBox(r.textures, r.meshes, r.shaders)

    globalState.cullFaces = false
  }

  fun render() {
    prepareRender()
    renderWorldMesh()
    renderElements()
  }

}

package rendering

import mythic.drawing.Canvas
import mythic.drawing.createDrawingMeshes
import mythic.drawing.getUnitScaling
import mythic.glowing.DrawMethod
import mythic.glowing.Glow
import mythic.platforming.WindowInfo
import mythic.glowing.SimpleMesh
import mythic.glowing.Texture
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.typography.loadFonts
import org.joml.Vector2i
import scenery.Scene
import mythic.spatial.Vector4
import mythic.typography.FontLoadInfo
import mythic.typography.TextConfiguration

fun gatherEffectsData(windowInfo: WindowInfo, scene: Scene): EffectsData {
  return EffectsData(
      createCameraMatrix(windowInfo.dimensions, scene.camera),
      Matrix().ortho(0.0f, windowInfo.dimensions.x.toFloat(), 0.0f, windowInfo.dimensions.y.toFloat(), 0f, 100f)
  )
}

fun renderScene(scene: Scene, painters: Painters, effects: Effects, textures: Textures, worldMesh: WorldMesh?) {
  if (worldMesh != null) {
    effects.textured.activate(Matrix(), textures.checkers)
    var index = 0
    for (texture in worldMesh.textureIndex) {
      texture.activate()
      worldMesh.mesh.drawElement(DrawMethod.triangleFan, index++)
    }
  }

  for (element in scene.elements) {
    painters[element.depiction]!!(element, effects)
  }
}

data class WorldMesh(
    val mesh: SimpleMesh,
    val textureIndex: List<Texture>
)

class Renderer {
  val glow = Glow()
  val shaders = createShaders()
  val vertexSchemas = createVertexSchemas()
  val meshes = createMeshes(vertexSchemas)
  var worldMesh: WorldMesh? = null
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val painters = createPainters(meshes)
  val textures = Textures()
  val fonts = loadFonts(listOf(
      FontLoadInfo("cour.ttf", 16, 0f)
  ))

  init {
//    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    glow.state.clearColor = Vector4(1f, 0.95f, 0.9f, 1f)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
  }

  fun renderScene(scene: Scene, windowInfo: WindowInfo) {
    val effects = createEffects(shaders, gatherEffectsData(windowInfo, scene))
    renderScene(scene, painters, effects, textures, worldMesh)
  }

}
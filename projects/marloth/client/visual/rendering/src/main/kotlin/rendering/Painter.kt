package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.checkError
import mythic.glowing.globalState
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.getRotationMatrix
import mythic.typography.TextStyle
import rendering.meshes.Primitives
import scenery.*

//typealias Painter = (VisualElement, Effects, ElementDetails) -> Unit
//typealias Painters = Map<DepictionType, Painter>

fun advancedPainter(mesh: AdvancedModel, renderer: Renderer, element: VisualElement, effects: Shaders) {
  val transform = element.transform.rotateZ(Pi / 2).scale(2f)
  val orientationTransform = getRotationMatrix(transform)
  for (e in mesh.primitives) {
    val material = e.material
    val boneBuffer = if (element.animation != null)
      populateBoneBuffer(renderer.boneBuffer, element.animation!!.armature.bones)
    else
      null

    val shaderConfig = ObjectShaderConfig(
        transform = transform,
        glow = material.glow,
        normalTransform = orientationTransform,
        color = material.color,
        texture = renderer.textures[Textures.checkers]!!,
        boneBuffer = boneBuffer
    )
    effects.animated.activate(shaderConfig)
//        effects.flatAnimated.activate(shaderConfig)
    checkError("drawing animated mesh-pre")
    e.mesh.draw(DrawMethod.triangleFan)
    checkError("drawing animated mesh")
  }
}

fun simplePainter(elements: Primitives, element: VisualElement, effects: Shaders) {
  val orientationTransform = getRotationMatrix(element.transform)
  for (e in elements) {
    val material = e.material
    effects.colored.activate(ObjectShaderConfig(
        element.transform,
        color = material.color,
        glow = material.glow,
        normalTransform = orientationTransform
    ))
    e.mesh.draw(DrawMethod.triangleFan)
  }
}

fun humanPainter(renderer: SceneRenderer, elements: Primitives) =
    { element: VisualElement, effects: Shaders, childDetails: ChildDetails ->
      val transform = element.transform.rotateZ(Pi / 2).scale(2f)
      val orientationTransform = getRotationMatrix(element.transform)
      val gender = childDetails.gender
      val commonParts = listOf("child", "eyes", "face")
      val girlParts = listOf("hair", "leaf_dress")
      val boyParts = listOf("boy-hair", "boy-clothes")
      val parts = commonParts.plus(if (gender == Gender.female)
        girlParts
      else
        boyParts
      )

      for (e in elements.filter { parts.contains(it.name) }) {
        val material = e.material
//        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
//        e.mesh.draw(DrawMethod.triangleFan)
      }

      val textStyle = TextStyle(renderer.renderer.fonts[0], 12f, Vector4(1f))
      globalState.depthEnabled = false
      val position = element.transform.getTranslation(Vector3())

//      renderer.drawText("Hello World", position, textStyle)
      globalState.depthEnabled = true
    }

val simplePainterMap = mapOf(
    DepictionType.monster to MeshType.eyeball,
    DepictionType.character to MeshType.child,
    DepictionType.missile to MeshType.sphere,
    DepictionType.wallLamp to MeshType.wallLamp
)
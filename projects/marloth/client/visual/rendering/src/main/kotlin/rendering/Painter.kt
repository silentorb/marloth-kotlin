package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.checkError
import mythic.glowing.globalState
import mythic.spatial.Pi
import mythic.spatial.Vector4
import mythic.spatial.getRotationMatrix
import mythic.typography.TextStyle
import org.joml.times
import rendering.meshes.Primitives
import scenery.*

//typealias Painter = (VisualElement, Effects, ElementDetails) -> Unit
//typealias Painters = Map<DepictionType, Painter>

fun advancedPainter(mesh: AdvancedModel, renderer: Renderer, element: MeshElement, effects: Shaders) {
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
        texture = renderer.mappedTextures[Textures.checkers]!!,
        boneBuffer = boneBuffer
    )
    effects.textured.activate(shaderConfig)
//        effects.flatAnimated.activate(shaderConfig)
    checkError("drawing animated mesh-pre")
    e.mesh.draw(DrawMethod.triangleFan)
    checkError("drawing animated mesh")
  }
}

fun simplePainter(elements: Primitives, element: MeshElement, effects: Shaders, textures: DynamicTextureLibrary) {
  for (e in elements) {
    val transform = if (e.transform != null)
      element.transform * e.transform
    else
      element.transform

    val orientationTransform = getRotationMatrix(transform)
    val material = e.material
    val texture = textures[material.texture]

    val config = ObjectShaderConfig(
        transform,
        color = material.color,
        glow = material.glow,
        normalTransform = orientationTransform,
        texture = texture
    )
    val effect = if (texture != null)
      effects.textured
    else
      effects.colored

    effect.activate(config)
    e.mesh.draw(DrawMethod.triangleFan)
  }
}

fun humanPainter(renderer: SceneRenderer, elements: Primitives) =
    { element: MeshElement, effects: Shaders, childDetails: ChildDetails ->
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
//      val position = element.transform.getTranslation(Vector3())

//      renderer.drawText("Hello World", position, textStyle)
      globalState.depthEnabled = true
    }

package rendering

import mythic.breeze.transformSkeleton
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

//
//fun humanPainter(renderer: SceneRenderer, elements: Primitives) =
//    { element: MeshElement, effects: Shaders, childDetails: ChildDetails ->
//      val transform = element.transform.rotateZ(Pi / 2).scale(2f)
//      val orientationTransform = getRotationMatrix(element.transform)
//      val gender = childDetails.gender
//      val commonParts = listOf("child", "eyes", "face")
//      val girlParts = listOf("hair", "leaf_dress")
//      val boyParts = listOf("boy-hair", "boy-clothes")
//      val parts = commonParts.plus(if (gender == Gender.female)
//        girlParts
//      else
//        boyParts
//      )
//
//      for (e in elements.filter { parts.contains(it.name) }) {
//        val material = e.material
////        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
////        e.mesh.draw(DrawMethod.triangleFan)
//      }
//
//      val textStyle = TextStyle(renderer.renderer.fonts[0], 12f, Vector4(1f))
//      globalState.depthEnabled = false
////      val position = element.transform.getTranslation(Vector3())
//
////      renderer.drawText("Hello World", position, textStyle)
//      globalState.depthEnabled = true
//    }

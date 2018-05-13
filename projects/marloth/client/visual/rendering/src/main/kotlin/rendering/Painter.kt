package rendering

import mythic.glowing.DrawMethod
import mythic.spatial.Pi
import mythic.spatial.getRotationMatrix
import rendering.meshes.MeshMap
import rendering.meshes.ModelElements
import scenery.ChildDetails
import scenery.DepictionType
import scenery.Gender
import scenery.VisualElement

//typealias Painter = (VisualElement, Effects, ElementDetails) -> Unit
//typealias Painters = Map<DepictionType, Painter>

fun simplePainter(elements: ModelElements) =
    { element: VisualElement, effects: Effects ->
      val orientationTransform = getRotationMatrix(element.transform)
      for (e in elements) {
        val material = e.material
        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
        e.mesh.draw(DrawMethod.triangleFan)
      }
    }

fun humanPainter(elements: ModelElements) =
    { element: VisualElement, effects: Effects, childDetails: ChildDetails ->
      val transform = element.transform.rotateZ(Pi / 2).scale(2f)
      val orientationTransform = getRotationMatrix(element.transform)
      val gender = childDetails.gender
      val commonParts = listOf("child", "eyes")
      val girlParts = listOf("hair", "leaf_dress")
      val boyParts = listOf("boy-hair", "boy-clothes")
      val parts = commonParts.plus(if (gender == Gender.female)
        girlParts
      else
        boyParts
      )

      for (e in elements.filter { parts.contains(it.name) }) {
        val material = e.material
        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
        e.mesh.draw(DrawMethod.triangleFan)
      }
    }

val simplePainterMap = mapOf(
    DepictionType.monster to MeshType.child,
    DepictionType.character to MeshType.child,
    DepictionType.missile to MeshType.sphere,
    DepictionType.wallLamp to MeshType.wallLamp
)

//fun createPainters(meshes: MeshMap): Painters = mapOf(
//    DepictionType.monster to humanPainter(meshes[MeshType.child]!!),
//    DepictionType.character to humanPainter(meshes[MeshType.child]!!),
//    DepictionType.missile to simplePainter(meshes[MeshType.sphere]!!),
//    DepictionType.wallLamp to simplePainter(meshes[MeshType.wallLamp]!!)
//)
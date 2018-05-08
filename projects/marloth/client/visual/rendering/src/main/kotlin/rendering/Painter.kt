package rendering

import mythic.glowing.DrawMethod
import mythic.spatial.Pi
import mythic.spatial.getRotationMatrix
import rendering.meshes.MeshMap
import rendering.meshes.ModelElements
import scenery.DepictionType
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<DepictionType, Painter>

fun simplePainter(elements: ModelElements): Painter =
    { element, effects ->
      val orientationTransform = getRotationMatrix(element.transform)
      for (e in elements) {
        val material = e.material
        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
        e.mesh.draw(DrawMethod.triangleFan)
      }
    }

fun humanPainter(elements: ModelElements): Painter =
    { element, effects ->
      val transform = element.transform.rotateZ(Pi / 2).scale(2f)
      val orientationTransform = getRotationMatrix(element.transform)
      for (e in elements.filter {it.name != "boy-clothes" && it.name != "boy-hair"}) {
        val material = e.material
        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
        e.mesh.draw(DrawMethod.triangleFan)
      }
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    DepictionType.monster to humanPainter(meshes[MeshType.child]!!),
    DepictionType.character to humanPainter(meshes[MeshType.child]!!),
    DepictionType.missile to simplePainter(meshes[MeshType.sphere]!!),
    DepictionType.wallLamp to simplePainter(meshes[MeshType.wallLamp]!!)
)
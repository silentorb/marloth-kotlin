package rendering

import mythic.glowing.DrawMethod
import mythic.spatial.getRotationMatrix
import rendering.meshes.MeshMap
import rendering.meshes.ModelElements
import scenery.DepictionType
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<DepictionType, Painter>

fun createSimplePainter(elements: ModelElements): Painter =
    { element, effects ->
      val orientationTransform = getRotationMatrix(element.transform)
      for (e in elements) {
        val material = e.material
        effects.colored.activate(element.transform, material.color, material.glow, orientationTransform)
        e.mesh.draw(DrawMethod.triangleFan)
      }
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    DepictionType.monster to createSimplePainter(meshes[MeshType.bear]!!),
    DepictionType.character to createSimplePainter(meshes[MeshType.cube]!!),
    DepictionType.missile to createSimplePainter(meshes[MeshType.sphere]!!),
    DepictionType.wallLamp to createSimplePainter(meshes[MeshType.wallLamp]!!)
)
package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.SimpleMesh
import mythic.spatial.Vector4
import mythic.spatial.getRotationMatrix
import scenery.DepictionType
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<DepictionType, Painter>

fun createSimplePainter(mesh: SimpleMesh): Painter =
    { element, effects ->
      val orientationTransform = getRotationMatrix(element.transform)
      effects.colored.activate(element.transform, Vector4(1f, 1f, 1f, 1f), orientationTransform)
      mesh.draw(DrawMethod.triangleFan)
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    DepictionType.character to createSimplePainter(meshes["cylinder"]!!),
    DepictionType.missile to createSimplePainter(meshes["sphere"]!!)
)
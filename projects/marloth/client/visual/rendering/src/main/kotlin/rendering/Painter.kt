package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.SimpleMesh
import mythic.spatial.Vector4
import mythic.spatial.getRotationMatrix
import scenery.DepictionType
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<DepictionType, Painter>

fun createSimplePainter(mesh: SimpleMesh, color: Vector4 = Vector4(1f, 1f, 1f, 1f)): Painter =
    { element, effects ->
      val orientationTransform = getRotationMatrix(element.transform)
      effects.colored.activate(element.transform, color, orientationTransform)
      mesh.draw(DrawMethod.triangleFan)
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    DepictionType.character to createSimplePainter(meshes[MeshType.character]!!, Vector4(0.3f, 0.2f, 1.0f, 1f)),
    DepictionType.missile to createSimplePainter(meshes[MeshType.sphere]!!,Vector4(0.4f, 0.1f, 0.1f, 1f))
)
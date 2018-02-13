package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.SimpleMesh
import mythic.spatial.Vector4
import scenery.Depiction
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<Depiction, Painter>

fun createSimplePainter(mesh: SimpleMesh): Painter =
    { element, effects ->
      effects.flat.activate(element.transform, Vector4(1f, 1f, 0f, 1f))
      mesh.draw(DrawMethod.lineLoop)
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    Depiction.child to createSimplePainter(meshes["cylinder"]!!)
)
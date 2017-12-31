package rendering

import mythic.glowing.DrawMethod
import mythic.glowing.SimpleMesh
import scenery.Depiction
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<Depiction, Painter>

fun createSimplePainter(mesh: SimpleMesh): Painter =
    { element, effects ->
      effects.standard.activate(element.transform)
      mesh.draw(DrawMethod.lineLoop)
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    Depiction.child to createSimplePainter(meshes["child"]!!)
)
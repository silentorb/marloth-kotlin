package rendering

import glowing.DrawMethod
import glowing.SimpleMesh
import scenery.Depiction
import scenery.VisualElement

typealias Painter = (VisualElement, Effects) -> Unit
typealias Painters = Map<Depiction, Painter>

fun createSimplePainter(mesh: SimpleMesh): Painter =
    { element, effects ->
      effects.standardEffect.activate()
      mesh.draw(DrawMethod.lineLoop)
    }

fun createPainters(meshes: MeshMap): Painters = mapOf(
    Depiction.child to createSimplePainter(meshes["child"]!!)
)
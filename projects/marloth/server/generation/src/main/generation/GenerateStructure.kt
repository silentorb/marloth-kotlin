package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import org.joml.xy

fun createNodeStructure(node: Node, mesh: HalfEdgeMesh) {
  for (connection in node.connections) {
    val other = connection.getOther(node)
    val point = node.position.xy + node.position.xy.normalize() * node.radius
    mesh.addVertex(Vector3(point.x, point.y, 0f))
  }
}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  abstractWorld.nodes.forEach { createNodeStructure(it, structureWorld.mesh) }
}
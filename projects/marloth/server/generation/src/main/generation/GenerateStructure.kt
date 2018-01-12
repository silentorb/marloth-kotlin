package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.times
import mythic.spatial.toVector3
import org.joml.minus
import org.joml.plus
import org.joml.xy

fun createDoorway(node: Node, other: Node, mesh: HalfEdgeMesh) {
  val direction = (other.position - node.position).xy.normalize()
  val point = node.position.xy + direction * node.radius
  val points = forkVector(point, direction, 1.5f)
  mesh.addVertex(points.first.toVector3())
  mesh.addVertex(points.second.toVector3())
}

fun createVerticesForOverlappingCircles(node: Node, other: Node, mesh: HalfEdgeMesh) {
  val points = circleIntersection(node.position.xy, node.radius, other.position.xy, other.radius)
  mesh.addVertex(points.first.toVector3())
  mesh.addVertex(points.second.toVector3())
}

fun createNodeStructure(node: Node, mesh: HalfEdgeMesh) {
  for (connection in node.connections) {
    val other = connection.getOther(node)
    if (connection.type != ConnectionType.union) {
      createDoorway(node, other, mesh)
    } else {
      createVerticesForOverlappingCircles(node, other, mesh)
    }
  }
}

fun generateStructure(abstractWorld: AbstractWorld, structureWorld: StructureWorld) {
  abstractWorld.nodes.forEach { createNodeStructure(it, structureWorld.mesh) }
}
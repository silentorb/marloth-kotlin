package rendering

import glowing.VertexAttribute
import glowing.VertexSchema

fun createMeshes(): MeshMap {
  val vertexSchema = VertexSchema(arrayOf(
      VertexAttribute(0, "position", 3),
      VertexAttribute(1, "normal", 3),
      VertexAttribute(2, "color", 4)
  ))
  val newMeshes = createMeshes(vertexSchema)
  return newMeshes.mapValues({ (_, m) -> convertMesh(m.mesh, m.vertexSchema) })
}

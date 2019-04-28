package mythic.sculpting

fun serializeFaces(faces: List<ImmutableFace>): String {
  val vertices = faces.flatMap { it.vertices }.distinct()
  val vertexClause = vertices
      .map { "(${it.x}, ${it.y}, ${it.z})" }
      .joinToString(", ")

  val indexClause = faces
      .map { face ->
        val indices = face.vertices
            .map { vertices.indexOf(it).toString() }
            .joinToString(", ")
        "($indices)"
      }
      .joinToString(", ")

  return """
import bpy
mesh = bpy.context.object.data
vertices = [$vertexClause]
indices = [$indexClause]
mesh.from_pydata(vertices, [], indices)
mesh.update(calc_edges=True)
  """.trimIndent()
}

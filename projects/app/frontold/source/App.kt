import rendering.MeshMap
import rendering.MeshReference
import rendering.convertMesh
import rendering.createMeshes
import shading.Vertex_Attribute
import shading.Vertex_Schema

fun initializeMeshes(mythic: MythicInterface): MeshMap {
  val vertexSchema = Vertex_Schema(listOf(
      Vertex_Attribute(0, "position", 3)
  ))
  val newMeshes = createMeshes(vertexSchema)
  return newMeshes.mapValues({ (n, m) ->
    val meshBuffer = convertMesh(m.mesh, m.vertexSchema)
    val id = mythic.createMesh(meshBuffer)
    meshBuffer.clear()
    MeshReference(n, id)
  })
}

class App {

  companion object {

    @JvmStatic
    fun main(args: List<String>) {
      val mythic = MythicInterface()
      mythic.initialize()
      val meshes = initializeMeshes(mythic)
      mythic.loop()
      mythic.shutdown()
    }
  }


}

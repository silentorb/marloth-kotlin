package rendering.meshes

import mythic.sculpting.*
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import rendering.FaceTextureMap
import rendering.Material
import rendering.Model
import rendering.mapMaterialToMesh

val createCube = {
  val mesh = ImmutableMesh()
  createCube(mesh, Vector3(1f, 1f, 1f))
  Model(
      mesh = mesh,
      groups = listOf(mapMaterialToMesh(Material(Vector4(.5f, .5f, .5f, 1f)), mesh))
  )
}

val createSkybox = {
  val mesh = ImmutableMesh()
  createCube(mesh, Vector3(1f, 1f, 1f))
  val uvs = listOf(
      Vector2(0f, 0f),
      Vector2(1f, 0f),
      Vector2(1f, 1f),
      Vector2(0f, 1f)
  )
  val textureMap: FaceTextureMap = mesh.faces.values.associate { face ->
    val vertexMap = face.vertices.zip(uvs) { a, b -> Pair(a, VertexNormalTexture(face.normal, b)) }.associate { it }
    Pair(face.id, vertexMap)
  }
  mesh.faces.values.forEach { it.flipQuad() }
  Model(
      mesh = mesh,
      textureMap = textureMap
  )
}

//val createSphere = {
//  val mesh = ImmutableMesh()
//  createSphere(mesh, 0.3f, 8, 6)
//  translateMesh(mesh, Vector3(0f, 0f, 1f))
//  Model(
//      mesh = mesh,
//      groups = listOf(mapMaterialToMesh(Material(Vector4(0.4f, 0.1f, 0.1f, 1f)), mesh))
//  )
//}
//
//fun createCylinder(): ImmutableMesh {
//  val mesh = ImmutableMesh()
//  mythic.sculpting.createCylinder(mesh, 0.5f, 8, 1f)
//  return mesh
//}

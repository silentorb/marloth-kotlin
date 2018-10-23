package rendering

import mythic.sculpting.ImmutableMesh
import mythic.sculpting.VertexNormalTexture
import mythic.sculpting.createCube
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import rendering.meshes.Primitive
import rendering.meshes.VertexSchemas
import rendering.meshes.convertMesh

fun skyboxModel(vertexSchemas: VertexSchemas): AdvancedModelGenerator = {
  val mesh = ImmutableMesh()
  createCube(mesh, Vector3(1f, 1f, 1f))
  val uvs = listOf(
      Vector2(0f, 0f),
      Vector2(1f, 0f),
      Vector2(1f, 1f),
      Vector2(0f, 1f)
  )
  val textureMap: FaceTextureMap = mesh.faces.associate { face ->
    val vertexMap = face.vertices.zip(uvs) { a, b -> Pair(a, VertexNormalTexture(face.normal, b)) }.associate { it }
    Pair(face, vertexMap)
  }
  mesh.faces.forEach { it.flipQuad() }
  val model = Model(
      mesh = mesh,
      textureMap = textureMap
  )
  val serializer = texturedVertexSerializer(textureMap)
  val primitives = listOf(Primitive(convertMesh(model.mesh.faces, vertexSchemas.textured, serializer), Material()))

  AdvancedModel(
      model = model,
      primitives = primitives
  )
}

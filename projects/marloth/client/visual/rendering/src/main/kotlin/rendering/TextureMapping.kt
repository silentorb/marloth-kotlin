package rendering

import mythic.sculpting.ImmutableFace
import mythic.sculpting.VertexNormalTexture
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.meshes.ImmutableVertexSerializer
import scenery.Textures

typealias VertexMap = Map<Vector3, VertexNormalTexture>
typealias FaceTextureMap = Map<ImmutableFace, VertexMap>

data class TextureFace(
    val face: ImmutableFace,
    val vertexMap: VertexMap,
    val texture: Textures
)

fun texturedVertexSerializer(vertexInfo: FaceTextureMap): ImmutableVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}
package rendering.texturing

import mythic.sculpting.VertexNormalTexture
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.meshes.ImmutableVertexSerializer
import silentorb.mythic.scenery.TextureName

typealias VertexMap = Map<Vector3, VertexNormalTexture>
typealias FaceTextureMap = Map<Long, VertexMap>

data class TextureFace(
    val face: Long,
    val vertexMap: VertexMap,
    val texture: TextureName
)

fun texturedVertexSerializer(vertexInfo: FaceTextureMap): ImmutableVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face.id]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}

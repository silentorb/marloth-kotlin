package rendering

import mythic.glowing.Texture
import mythic.sculpting.FlexibleFace
import mythic.sculpting.VertexNormalTexture
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.meshes.FlexibleVertexSerializer

typealias VertexMap = Map<Vector3, VertexNormalTexture>
typealias FaceTextureMap = Map<FlexibleFace, VertexMap>

data class TextureFace(
    val face: FlexibleFace,
    val vertexMap: VertexMap,
    val texture: Texture
)

fun texturedVertexSerializer(vertexInfo: FaceTextureMap): FlexibleVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}
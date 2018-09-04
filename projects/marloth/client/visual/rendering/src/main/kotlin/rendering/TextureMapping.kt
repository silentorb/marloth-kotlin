package rendering

import mythic.sculpting.FlexibleFace
import mythic.sculpting.VertexNormalTexture
import mythic.spatial.Vector3m
import mythic.spatial.put
import rendering.meshes.FlexibleVertexSerializer
import scenery.Textures

typealias VertexMap = Map<Vector3m, VertexNormalTexture>
typealias FaceTextureMap = Map<FlexibleFace, VertexMap>

data class TextureFace(
    val face: FlexibleFace,
    val vertexMap: VertexMap,
    val texture: Textures
)

fun texturedVertexSerializer(vertexInfo: FaceTextureMap): FlexibleVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}
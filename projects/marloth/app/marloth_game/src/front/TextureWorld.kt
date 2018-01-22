package front

import marloth.clienting.Client
import mythic.glowing.Texture
import mythic.sculpting.HalfEdgeFace
import mythic.sculpting.HalfEdgeVertex
import mythic.sculpting.VertexNormalTexture
import mythic.sculpting.query.getBounds
import mythic.sculpting.query.getVertices
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.*
import simulation.MetaWorld

typealias VertexMap = Map<HalfEdgeVertex, VertexNormalTexture>
typealias VertexInfo = Map<HalfEdgeFace, VertexMap>

data class TextureFace(
    val face: HalfEdgeFace,
    val vertexMap: VertexMap,
    val texture: Texture
)

fun createTexturedFloor(face: HalfEdgeFace, texture: Texture): TextureFace {
  val vertices = getVertices(face)
  val bounds = getBounds(vertices)
  val dimensions = bounds.dimensions
//    val scaleX = 1 / dimensions.x
//    val scaleY = 1 / dimensions.y
  val scaleX = .5f
  val scaleY = .5f
  return TextureFace(face, vertices.associate { vertex ->
    Pair(vertex, VertexNormalTexture(
        Vector3(0f, 0f, 1f),
        Vector2(
            (vertex.position.x - bounds.start.x) * scaleX,
            (vertex.position.y - bounds.start.y) * scaleY
        )
    ))
  },
      texture
  )
}

fun createTexturedWall(face: HalfEdgeFace, texture: Texture): TextureFace {
  val vertices = getVertices(face)
  val bounds = getBounds(vertices)
  val dimensions = bounds.dimensions
  val scaleX = .5f
  val scaleY = .5f
  val edge = face.edge!!
  val length = edge.vertex.position.distance(edge.next!!.vertex.position) * scaleX
  val uvs = listOf(
      Vector2(0f, 0f),
      Vector2(length, 0f),
      Vector2(length, 1f),
      Vector2(0f, 1f)
  ).listIterator()
  return TextureFace(face, vertices.associate { vertex ->
    Pair(vertex, VertexNormalTexture(
        Vector3(0f, 0f, 1f),
        uvs.next()
    ))
  },
      texture
  )
}

fun prepareWorldMesh(metaWorld: MetaWorld, textures: Textures): List<TextureFace> {
  return metaWorld.groups.floors.map { createTexturedFloor(it, textures.checkers) }
      .plus(
          metaWorld.groups.walls.map { createTexturedWall(it, textures.darkCheckers) }
      )
}

fun texturedVertexSerializer(vertexInfo: VertexInfo): VertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}

fun convertWorldMesh(metaWorld: MetaWorld, renderer: Renderer): WorldMesh {
  val texturedFaces = prepareWorldMesh(metaWorld, renderer.textures)
  val vertexInfo = texturedFaces.associate { Pair(it.face, it.vertexMap) }
  val serializer = texturedVertexSerializer(vertexInfo)
  return WorldMesh(
      convertMesh(metaWorld.structureWorld.mesh, renderer.vertexSchemas.textured, serializer),
      texturedFaces.map { it.texture }
  )
}

fun setWorldMesh(metaWorld: MetaWorld, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(metaWorld, client.renderer)
}
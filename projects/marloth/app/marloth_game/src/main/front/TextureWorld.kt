package main.front

import marloth.clienting.Client
import mythic.glowing.Texture
import mythic.sculpting.FlexibleFace
import mythic.sculpting.VertexNormalTexture
import mythic.sculpting.query.getBounds
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.*
import rendering.meshes.FlexibleVertexSerializer
import rendering.meshes.convertMesh
import simulation.AbstractWorld

typealias VertexMap = Map<Vector3, VertexNormalTexture>
typealias VertexInfo = Map<FlexibleFace, VertexMap>

data class TextureFace(
    val face: FlexibleFace,
    val vertexMap: VertexMap,
    val texture: Texture
)

fun createTexturedFloor(face: FlexibleFace, texture: Texture): TextureFace {
  val vertices = face.unorderedVertices
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
            (vertex.x - bounds.start.x) * scaleX,
            (vertex.y - bounds.start.y) * scaleY
        )
    ))
  },
      texture
  )
}

fun createTexturedWall(face: FlexibleFace, texture: Texture): TextureFace {
  val vertices = face.unorderedVertices
  val bounds = getBounds(vertices)
  val dimensions = bounds.dimensions
  val scaleX = .5f
  val scaleY = .5f
  val edge = face.edges.first()
  val length = edge.first.distance(edge.second) * scaleX
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

fun prepareWorldMesh(metaWorld: AbstractWorld, textures: Textures): List<TextureFace> {
  return metaWorld.floors.map { createTexturedFloor(it, textures.checkers) }
      .plus(
          metaWorld.walls.map { createTexturedWall(it, textures.darkCheckers) }
      )
}

fun texturedVertexSerializer(vertexInfo: VertexInfo): FlexibleVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}

fun convertWorldMesh(abstractWorld: AbstractWorld, renderer: Renderer): WorldMesh {
  val texturedFaces = prepareWorldMesh(abstractWorld, renderer.textures)
  val vertexInfo = texturedFaces.associate { Pair(it.face, it.vertexMap) }
  val serializer = texturedVertexSerializer(vertexInfo)
  return WorldMesh(
      convertMesh(abstractWorld.mesh, renderer.vertexSchemas.textured, serializer),
      texturedFaces.map { it.texture }
  )
}

fun setWorldMesh(abstractWorld: AbstractWorld, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(abstractWorld, client.renderer)
}
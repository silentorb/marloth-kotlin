package front

import generation.structure.getWallVertices
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
import simulation.Node
import simulation.getFaceInfo
import kotlin.math.roundToInt

typealias VertexMap = Map<Vector3, VertexNormalTexture>
typealias VertexInfo = Map<FlexibleFace, VertexMap>

data class TextureFace(
    val face: FlexibleFace,
    val vertexMap: VertexMap,
    val texture: Texture
)

fun createTexturedHorizontalSurface(face: FlexibleFace, texture: Texture): TextureFace {
  val vertices = face.unorderedVertices
  val bounds = getBounds(vertices)
  val scale = 2f
  val offset = Vector2(
      (bounds.start.x / scale).roundToInt().toFloat() * scale,
      (bounds.start.y / scale).roundToInt().toFloat() * scale
  )
  val scaleX = 1f / scale
  val scaleY = 1f / scale
  return TextureFace(face, vertices.associate { vertex ->
    Pair(vertex, VertexNormalTexture(
        Vector3(0f, 0f, 1f),
        Vector2(
            (vertex.x - offset.x) * scaleX,
            (vertex.y - offset.y) * scaleY
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
  val scale = .5f
  val edge = getWallVertices(face).upper
  val length = edge.first().distance(edge.last()) * scale
  val uvs = listOf(
      Vector2(0f, 0f),
      Vector2(length, 0f),
      Vector2(length, 2f),
      Vector2(0f, 2f)
  )
  val alignedUvs = if (vertices[0].z != vertices[1].z)
    listOf(uvs.last()).plus(uvs.dropLast(1))
  else
    uvs

  val uvIterator = alignedUvs.listIterator()
  return TextureFace(face, vertices.associate { vertex ->
    Pair(vertex, VertexNormalTexture(
        Vector3(0f, 0f, 1f),
        uvIterator.next()
    ))
  },
      texture
  )
}

fun prepareWorldMesh(node: Node, textures: TextureLibrary): List<TextureFace> {
//  val floorTexture = if (node.type == NodeType.space) textures[Textures.darkCheckers]!! else textures[Textures.checkers]!!
  val floors = node.floors
      .filter { getFaceInfo(it).firstNode == node && getFaceInfo(it).texture != null }

  val ceilings = node.ceilings
      .filter { getFaceInfo(it).firstNode == node && getFaceInfo(it).texture != null }

  return floors.plus(ceilings)
      .map { createTexturedHorizontalSurface(it, textures[getFaceInfo(it).texture]!!) }
      .plus(
          node.walls
              .filter { getFaceInfo(it).firstNode == node && getFaceInfo(it).texture != null }
              .map { createTexturedWall(it, textures[getFaceInfo(it).texture]!!) }
      )
}

fun texturedVertexSerializer(vertexInfo: VertexInfo): FlexibleVertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}

fun convertSectorMesh(renderer: Renderer, node: Node): SectorMesh {
  val texturedFaces = prepareWorldMesh(node, renderer.textures)
  val vertexInfo = texturedFaces.associate { Pair(it.face, it.vertexMap) }
  val serializer = texturedVertexSerializer(vertexInfo)
  return SectorMesh(
      convertMesh(texturedFaces.map { it.face }, renderer.vertexSchemas.textured, serializer),
      texturedFaces.map { it.texture }
  )
}

fun convertWorldMesh(abstractWorld: AbstractWorld, renderer: Renderer): WorldMesh {
  val sectors = abstractWorld.nodes.map {
    convertSectorMesh(renderer, it)
  }
  return WorldMesh(sectors)
}

fun setWorldMesh(abstractWorld: AbstractWorld, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(abstractWorld, client.renderer)
}
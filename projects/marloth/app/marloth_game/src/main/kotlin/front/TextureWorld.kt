package front

import generation.structure.getWallVertices
import marloth.clienting.Client
import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableFaceTable
import mythic.sculpting.VertexNormalTexture
import mythic.sculpting.getBounds
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import rendering.*
import rendering.meshes.convertMesh
import scenery.Textures
import simulation.*
import kotlin.math.roundToInt

fun createTexturedHorizontalSurface(face: ImmutableFace, texture: Textures): TextureFace {
  val vertices = face.unorderedVertices
  val bounds = getBounds(vertices)
  val scale = 2f
  val offset = Vector2(
      (bounds.start.x / scale).roundToInt().toFloat() * scale,
      (bounds.start.y / scale).roundToInt().toFloat() * scale
  )
  val scaleX = 1f / scale
  val scaleY = 1f / scale
  return TextureFace(face.id, vertices.associate { vertex ->
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

fun createTexturedWall(face: ImmutableFace, texture: Textures): TextureFace {
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
  return TextureFace(face.id, vertices.associate { vertex ->
    Pair(vertex, VertexNormalTexture(
        Vector3(0f, 0f, 1f),
        uvIterator.next()
    ))
  },
      texture
  )
}

fun prepareWorldMesh(realm: Realm, node: Node, textures: TextureLibrary): List<TextureFace> {
//  val floorTexture = if (node.type == NodeType.space) textures[Textures.darkCheckers]!! else textures[Textures.checkers]!!
  val floors = node.floors.map { Pair(realm.faces[it]!!, it) }
      .filter { (it, _) -> it.firstNode == node.id && it.texture != null }

  val ceilings = node.ceilings.map { Pair(realm.faces[it]!!, it) }
      .filter { (it, _) -> it.firstNode == node.id && it.texture != null }

  return floors.plus(ceilings)
      .map { createTexturedHorizontalSurface(realm.mesh.faces[it.second]!!, it.first.texture!!) }
      .plus(
          node.walls.map { Pair(realm.faces[it]!!, it) }
              .filter { (it, _) -> it.firstNode == node.id && it.texture != null }
              .map { createTexturedWall(realm.mesh.faces[it.second]!!, it.first.texture!!) }
      )
}

fun convertSectorMesh(realm: Realm, faces2: ImmutableFaceTable, renderer: Renderer, node: Node): SectorMesh {
  val texturedFaces = prepareWorldMesh(realm, node, renderer.mappedTextures)
  val vertexInfo = texturedFaces.associate { Pair(it.face, it.vertexMap) }
  val serializer = texturedVertexSerializer(vertexInfo)
  return SectorMesh(
      convertMesh(texturedFaces.map { faces2[it.face]!! }, renderer.vertexSchemas.textured, serializer),
      texturedFaces.map { it.texture }
  )
}

fun convertWorldMesh(realm: Realm, renderer: Renderer): WorldMesh {
  val faces2 = realm.mesh.faces.values.associate { Pair(it.id, it) }
  val sectors = realm.nodeList.map {
    convertSectorMesh(realm, faces2, renderer, it)
  }
  return WorldMesh(sectors)
}

fun setWorldMesh(realm: Realm, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(realm, client.renderer)
}
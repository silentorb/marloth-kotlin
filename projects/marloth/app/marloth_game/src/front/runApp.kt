package front

import generation.generateDefaultWorld
import marloth.clienting.Client
import mythic.glowing.SimpleMesh
import mythic.platforming.Platform
import mythic.quartz.DeltaTimer
import mythic.sculpting.Face
import mythic.sculpting.HalfEdgeMesh
import mythic.sculpting.Vertex
import mythic.sculpting.VertexNormalTexture
import mythic.sculpting.query.getBounds
import mythic.sculpting.query.getVertices
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.put
import rendering.Renderer
import rendering.VertexSerializer
import rendering.convertMesh
import simulation.MetaWorld
import simulation.StructureWorld
import simulation.updateWorld
import visualizing.createScene

fun runApp(platform: Platform) {
  val display = platform.display
  val timer = DeltaTimer()
  val world = generateDefaultWorld()
  val client = Client(platform)
  setWorldMesh(world.meta, client)

  while (!platform.process.isClosing()) {
    display.swapBuffers()
    val scene = createScene(world, client.screens[0])
    val commands = client.update(scene)
    val delta = timer.update().toFloat()
    updateWorld(world, commands, delta)
    platform.process.pollEvents()
  }
}

typealias VertexInfo = Map<Face, Map<Vertex, VertexNormalTexture>>

fun prepareWorldMesh(metaWorld: MetaWorld): VertexInfo {
  return metaWorld.groups.floors.associate { face ->
    val vertices = getVertices(face)
    val bounds = getBounds(vertices)
    val dimensions = bounds.dimensions
//    val scaleX = 1 / dimensions.x
//    val scaleY = 1 / dimensions.y
    val scaleX = .5f
    val scaleY = .5f
    Pair(face, vertices.associate { vertex ->
      Pair(vertex, VertexNormalTexture(
          Vector3(0f, 0f, 1f),
          Vector2(
              (vertex.position.x - bounds.start.x) * scaleX,
              (vertex.position.y - bounds.start.y) * scaleY
          )
      ))
    }
    )
  }
      .plus(
          metaWorld.groups.walls.associate { face ->
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
            Pair(face, vertices.associate { vertex ->
              Pair(vertex, VertexNormalTexture(
                  Vector3(0f, 0f, 1f),
                  uvs.next()
              ))
            }
            )
          }
      )
}

fun texturedVertexSerializer(vertexInfo: VertexInfo): VertexSerializer = { vertex, face, vertices ->
  val info = vertexInfo[face]!![vertex]!!
  vertices.put(info.normal)
  vertices.put(info.uv.x)
  vertices.put(info.uv.y)
}

fun convertWorldMesh(metaWorld: MetaWorld, renderer: Renderer): SimpleMesh {
  val vertexInfo = prepareWorldMesh(metaWorld)
  return convertMesh(metaWorld.structureWorld.mesh, renderer.vertexSchemas.textured, texturedVertexSerializer(vertexInfo))
}

fun setWorldMesh(metaWorld: MetaWorld, client: Client) {
  client.renderer.worldMesh = convertWorldMesh(metaWorld, client.renderer)
}
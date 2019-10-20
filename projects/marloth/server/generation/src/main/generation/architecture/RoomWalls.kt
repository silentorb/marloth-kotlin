package generation.architecture

import generation.misc.BiomeAttribute
import generation.misc.MeshAttribute
import generation.misc.queryMeshes
import mythic.spatial.Pi
import mythic.spatial.atan
import mythic.spatial.projectVector3
import simulation.misc.Graph
import simulation.misc.Node
import simulation.misc.nodeNeighbors2

fun getDoorwayAngles(graph: Graph, node: Node): List<Float> {
  val points = nodeNeighbors2(graph.connections, node.id)
      .map {
        val neighbor = graph.nodes[it]!!
        neighbor.position
      }

  return points
      .map { atan(it.xy() - node.position.xy()) }
      .sorted()
}

fun getRoomSeriesAngleLength(firstIndex: Int, stripCount: Int, doorwayAngles: List<Float>, firstAngle: Float): Float {
  val secondIndex = (firstIndex + 1) % stripCount
  val secondDoorway = doorwayAngles[secondIndex]
  val secondAngle = if (secondDoorway <= firstAngle)
    secondDoorway + Pi * 2f
  else
    secondDoorway

  return secondAngle - firstAngle
}

val placeRoomWalls: Architect = { config, realm, dice ->
  roomNodes(realm.graph)
      .flatMap { node ->
        val doorwayAngles = getDoorwayAngles(realm.graph, node)
        val biome = config.biomes[node.biome]!!

        val stripCount = doorwayAngles.size
        val tunnelAngleLength = getSegmentAngleLength(standardTunnelWidth, node.radius)
        val segmentAngleLength = getSegmentAngleLength(standardWallLength, node.radius)

        val wallSections = doorwayAngles.mapIndexed { index, firstAngle ->
          val angleLength = getRoomSeriesAngleLength(index, stripCount, doorwayAngles, firstAngle) - tunnelAngleLength
          Pair(firstAngle, angleLength)
        }
            .filter { it.second > segmentAngleLength }

        val slots = wallSections.flatMap { (firstAngle, angleLength) ->
          println("node " + node.id + " angleLength " + angleLength)
          if (node.id == 1L) {
            val k = 0
          }
          createOverlappingSeries(angleLength, segmentAngleLength) { step, stepOffset ->
            firstAngle + (tunnelAngleLength / 2f) + stepOffset
          }
        }

        val filteredSlots = slots.filter { wallPlacementFilter(dice, biome) }

        val windowIndex = if (filteredSlots.size > 2 &&
            (biome.attributes.contains(BiomeAttribute.alwaysWindow) || dice.getInt(0, 3) == 1))
          dice.getInt(0, filteredSlots.size - 1)
        else
          -1

        filteredSlots.mapIndexed { index, wallAngle ->
          val wallPosition = node.position + projectVector3(wallAngle, node.radius, node.position.z)
          val meshOptions = if (windowIndex == index)
            queryMeshes(config.meshes, biome.meshes, setOf(MeshAttribute.placementWindow))
          else
            queryMeshes(config.meshes, biome.meshes, setOf(MeshAttribute.placementWall))
          val mesh = dice.takeOne(meshOptions)
          newWall(config, mesh, dice, node, wallPosition, wallAngle)
        }
      }
}

package generation.architecture

import generation.misc.BiomeAttribute
import generation.misc.MeshAttribute
import generation.misc.MeshAttributes
import generation.misc.filterMeshes
import mythic.spatial.Pi
import mythic.spatial.atan
import mythic.spatial.projectVector3
import simulation.misc.*

fun getDoorwayAngles(graph: Graph, node: Node): List<Float> {
  val connectionPool = graph.connections
      .asSequence()
      .filter { it.type != ConnectionType.vertical }

  val points = nodeNeighbors2(connectionPool, node.id)
      .map { graph.nodes[it]!! }
      .map { it.position }

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
        val biome = config.biomes[node.biome]!!
        val tunnelAngleLength = getSegmentAngleLength(standardTunnelWidth, node.radius)
        val segmentAngleLength = getSegmentAngleLength(standardWallLength, node.radius)

        val doorwayAngles = getDoorwayAngles(realm.graph, node)
        val stripCount = doorwayAngles.size

        val slots = if (stripCount > 0) {
          val wallSections = doorwayAngles.mapIndexed { index, firstAngle ->
            val angleLength = getRoomSeriesAngleLength(index, stripCount, doorwayAngles, firstAngle) - tunnelAngleLength
            Pair(firstAngle, angleLength)
          }
              .filter { it.second > segmentAngleLength }
          wallSections.flatMap { (firstAngle, angleLength) ->
            createOverlappingSeries(angleLength, segmentAngleLength) { step, stepOffset ->
              firstAngle + (tunnelAngleLength / 2f) + stepOffset
            }
          }
        } else {
          createOverlappingSeries(Pi * 2f, segmentAngleLength) { step, stepOffset ->
            (tunnelAngleLength / 2f) + stepOffset
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
          val getMesh = filterMeshes(config.meshes, biome.meshes)
          val windowAttributes = setOf(MeshAttribute.placementWindow)
          val wallAttributes = setOf(MeshAttribute.placementWall)
          val attributes = listOf(
              if (windowIndex == index) windowAttributes else wallAttributes,
              wallAttributes
          )
          val meshes = attributes.map(getMesh).map { dice.takeOne(it) }
          newWall(config, meshes, node, wallPosition, wallAngle)
        }.flatten()
      }
}

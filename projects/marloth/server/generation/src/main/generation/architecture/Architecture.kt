package generation.architecture

import generation.misc.*
import mythic.ent.Id
import mythic.spatial.*
import randomly.Dice
import scenery.enums.MeshId
import simulation.entities.ArchitectureElement
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.WorldTransform
import simulation.main.addHands
import simulation.misc.*
import simulation.physics.old.getLookAtAngle
import kotlin.math.ceil

fun <T> createSeries(gapSize: Float, segmentLength: Float, margin: Float, action: (Int, Float) -> T): List<T> {
  val length = gapSize - margin * 2f
  val stepCount = ceil(length / segmentLength).toInt()
  val overflow = stepCount * segmentLength - length
  val stepSize = if (stepCount > 1) segmentLength - overflow / (stepCount - 1) else 0f
  val start = margin + segmentLength / 2f
  return (0 until stepCount).map { step ->
    val stepOffset = start + stepSize * step.toFloat()
    action(step, stepOffset)
  }
}

fun tunnelNodes(graph: Graph) = graph.nodes.values
    .filter { node -> graph.tunnels.contains(node.id) }

fun roomNodes(graph: Graph) = graph.nodes.values
    .filter { node -> !graph.tunnels.contains(node.id) }

typealias Architect = (config: GenerationConfig, Realm, Dice) -> List<Hand>

val placeRoomFloors: Architect = { config, realm, dice ->
  roomNodes(realm.graph)
      .map { node ->
        val floorMeshAdjustment = 1f / 4f
        val horizontalScale = (node.radius + 1f) * 2f * floorMeshAdjustment
        val biome = config.biomes[node.biome!!]!!
        val position = realm.cellMap[node.id]!!
        val isEmptyBelow = (1..4).none {
          realm.grid.connections.containsKey(position.copy(z = position.z - it))
              || realm.grid.cells.containsKey(position.copy(z = position.z - it))
        }
        val meshOptions = if (isEmptyBelow)
          biome.roomFloorMeshesTall
        else
          biome.roomFloorMeshes

        val mesh = dice.takeOne(meshOptions)
        newArchitectureMesh(
            architecture = ArchitectureElement(isWall = false),
            meshInfo = config.meshes,
            mesh = mesh,
            position = node.position + floorOffset + alignWithCeiling(config.meshes)(mesh),
            orientation = Quaternion(),
            node = node.id,
            texture = biome.floorTexture
        )
      }
}

data class TunnelInfo(
    val start: Vector3,
    val vector: Vector3,
    val length: Float
)

fun getTunnelInfo(graph: Graph, node: Id): TunnelInfo {
  val neighbors = nodeNeighbors2(graph.connections, node).map { graph.nodes[it]!! }
  val overlap = 1f
  val length = getNodeDistance(neighbors[0], neighbors[1]) + overlap
  val horizontalVector = (neighbors[0].position.copy(z = 0f) - neighbors[1].position.copy(z = 0f)).normalize()
  val start = neighbors[1].position + horizontalVector * neighbors[1].radius
  val end = neighbors[0].position - horizontalVector * neighbors[0].radius
  val vector = (end - start).normalize()

  return TunnelInfo(
      start = start,
      vector = vector,
      length = length
  )
}

fun getTunnelFloorMesh(dice: Dice, biome: BiomeInfo, info: TunnelInfo): MeshId {
  val options = if (Math.abs(info.vector.z) > 0.3f)
    biome.stairStepMeshes
  else
    biome.tunnelFloorMeshes

  return dice.takeOne(options)
}

val placeTunnelFloors: Architect = { config, realm, dice ->
  // Temporary improvement while the tunnel floor is rounded and the room floor isn't
  val tempHeightBump = 0.05f

  tunnelNodes(realm.graph)
      .flatMap { node ->
        val biome = config.biomes[node.biome]!!
        val info = getTunnelInfo(realm.graph, node.id)
        val mesh = getTunnelFloorMesh(dice, biome, info)
        val segmentLength = config.meshes[mesh.name]!!.shape.x
        val randomRotation1 = dice.getFloat(-0.1f, 0.1f)
        val randomRotation2 = dice.getFloat(-0.1f, 0.1f)
        val orientation = Quaternion()
            .rotateZ(getLookAtAngle(info.vector) + randomRotation1)
//            .rotateY(-getLookAtAngle(Vector2(info.vector.xy().length(), info.vector.z) + randomRotation2))
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod + tempHeightBump)
          newArchitectureMesh(
              architecture = ArchitectureElement(isWall = false),
              meshInfo = config.meshes,
              mesh = mesh,
              position = info.start + info.vector * stepOffset + floorOffset + minor + alignWithCeiling(config.meshes)(mesh),
              orientation = orientation,
              node = node.id,
              texture = biome.floorTexture
          )
        }

      }
}

fun wallPlacementFilter(dice: Dice, wallPlacement: WallPlacement) =
    when (wallPlacement) {
      WallPlacement.all -> true
      WallPlacement.none -> false
      else -> dice.getFloat() < 0.75f
    }

val placeTunnelWalls: Architect = { config, realm, dice ->
  tunnelNodes(realm.graph)
      .flatMap { node ->
        val segmentLength = 4f
        val info = getTunnelInfo(realm.graph, node.id)
        val lookAtAngle = getLookAtAngle(info.vector)
        val halfWidth = 2f
        val biome = config.biomes[node.biome]!!
        createSeries(info.length, segmentLength, -0f) { step, stepOffset ->
          val minorOffset = 0.001f
          val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
          val minor = Vector3(0f, 0f, minorMod)
          listOf(-1f, 1f)
              .filter { wallPlacementFilter(dice, biome.wallPlacement) }
              .map { sideMod ->
                val randomFlip = if (dice.getBoolean()) 1 else -1
                val sideOffset = Vector3(info.vector.y, -info.vector.x, 0f) * (sideMod + minorMod) * halfWidth
                val wallPosition = info.start + info.vector * stepOffset + sideOffset
                val wallAngle = lookAtAngle + sideMod * randomFlip * Pi / 2f
                val mesh = dice.takeOne(biome.wallMeshes)
                newWall(config, mesh, dice, node, wallPosition, wallAngle)
              }
        }.flatten()
      }
}

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

        val slots = doorwayAngles.mapIndexed { index, firstAngle ->
          val angleLength = getRoomSeriesAngleLength(index, stripCount, doorwayAngles, firstAngle)
          val segmentLength = 4f / node.radius
          if (angleLength < segmentLength) {
            listOf()
          } else {
            val margin = 1.6f / node.radius
            createSeries(angleLength, segmentLength, margin) { step, stepOffset ->
              firstAngle + stepOffset
            }
          }
        }.flatten()

        val filteredSlots = slots.filter { wallPlacementFilter(dice, biome.wallPlacement) }

        val windowIndex = if (filteredSlots.size > 2 &&
            (biome.attributes.contains(BiomeAttribute.alwaysWindow) || dice.getInt(0, 3) == 1))
          dice.getInt(0, filteredSlots.size - 1)
        else
          -1

        if (biome.attributes.contains(BiomeAttribute.alwaysWindow)) {
          val ka = 0
        }

        filteredSlots.mapIndexed { index, wallAngle ->
          val wallPosition = node.position + projectVector3(wallAngle, node.radius, node.position.z)
          val meshOptions = if (windowIndex == index)
            biome.windowMeshes
          else
            biome.wallMeshes
          val mesh = dice.takeOne(meshOptions)
          newWall(config, mesh, dice, node, wallPosition, wallAngle)
        }
      }
}

private val architectureSteps = listOf(
    placeRoomFloors,
    placeRoomWalls,
    placeTunnelFloors,
    placeTunnelWalls
)

fun placeArchitecture(config: GenerationConfig, realm: Realm, dice: Dice): List<Hand> =
    architectureSteps.flatMap { it(config, realm, dice) }

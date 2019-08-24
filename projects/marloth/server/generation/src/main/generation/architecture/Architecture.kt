package generation.architecture

import generation.misc.*
import mythic.spatial.*
import randomly.Dice
import simulation.entities.ArchitectureElement
import simulation.main.Hand
import simulation.misc.Graph
import simulation.misc.Realm
import simulation.physics.old.getLookAtAngle
import kotlin.math.ceil

const val standardTunnelWidth = 4.5f

fun <T> createSeries(gapSize: Float, segmentLength: Float, margin: Float = 0f, action: (Int, Float) -> T): List<T> {
  val length = gapSize - margin * 2f
  val stepCount = ceil(length / segmentLength).toInt()
  println(stepCount)
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
          queryMeshes(config.meshes, biome.meshes, setOf(MeshAttribute.placementTallFloor))
        else
          queryMeshes(config.meshes, biome.meshes, setOf(MeshAttribute.placementShortFloor))

        val mesh = dice.takeOne(meshOptions)
        newArchitectureMesh(
            architecture = ArchitectureElement(isWall = false),
            meshes = config.meshes,
            mesh = mesh,
            position = node.position + floorOffset + alignWithCeiling(config.meshes)(mesh),
            orientation = Quaternion(),
            node = node.id,
            texture = biome.floorTexture
        )
      }
}

fun getTunnelFloorMesh(biome: BiomeInfo, info: TunnelInfo): Set<MeshAttribute> {
  return if (Math.abs(info.vector.z) > 0.3f)
    setOf(MeshAttribute.placementStairStep)
  else
    setOf(MeshAttribute.placementTunnelFloor)
}

val placeTunnelFloors: Architect = { config, realm, dice ->
  // Temporary improvement while the tunnel floor is rounded and the room floor isn't
  val tempHeightBump = 0.05f

  tunnelNodes(realm.graph)
      .flatMap { node ->
        val biome = config.biomes[node.biome]!!
        val info = getTunnelInfo(realm.graph, node.id)
        val meshPool = queryMeshes(config.meshes, biome, getTunnelFloorMesh(biome, info))
        val mesh = dice.takeOne(meshPool)
        val segmentLength = config.meshes[mesh]!!.shape.x
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
              meshes = config.meshes,
              mesh = mesh,
              position = info.start + info.vector * stepOffset + floorOffset + minor + alignWithCeiling(config.meshes)(mesh),
              orientation = orientation,
              node = node.id,
              texture = biome.floorTexture
          )
        }

      }
}

const val standardWallLength = 4f

private val architectureSteps = listOf(
    placeRoomFloors,
    placeRoomWalls,
    placeTunnelFloors,
    placeTunnelWalls
)

fun placeArchitecture(config: GenerationConfig, realm: Realm, dice: Dice): List<Hand> =
    architectureSteps.flatMap { it(config, realm, dice) }

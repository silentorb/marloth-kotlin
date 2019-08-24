package generation.architecture

import generation.misc.*
import generation.structure.wallHeight
import mythic.ent.Id
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.physics.Body
import randomly.Dice
import scenery.MeshName
import scenery.enums.TextureId
import simulation.entities.ArchitectureElement
import simulation.physics.CollisionObject
import simulation.main.Hand
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.misc.Node
import kotlin.math.atan

fun getHorizontalFlip(dice: Dice, info: ArchitectureMeshInfo): Float =
    if (info.attributes.contains(MeshAttribute.canFlipHorizontally) && dice.getBoolean()) Pi else 0f

val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

fun newArchitectureMesh(meshes: MeshInfoMap, mesh: MeshName, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        node: Id,
                        architecture: ArchitectureElement,
                        texture: TextureId? = null, scale: Vector3 = Vector3.unit): Hand {
  val meshInfo = meshes[mesh]!!
  return Hand(
      architecture = architecture,
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = texture?.name
      ),
      body = Body(
          position = position,
          orientation = orientation,
          nearestNode = node,
          scale = scale
      ),
      collisionShape = CollisionObject(shape = meshInfo.shape)
  )
}

fun alignWithCeiling(meshInfo: MeshInfoMap) = { mesh: MeshName ->
  val height = meshInfo[mesh]!!.shape.height
  Vector3(0f, 0f, -height / 2f)
}

fun alignWithFloor(meshInfo: MeshInfoMap) = { mesh: MeshName ->
  val height = meshInfo[mesh]!!.shape.height
  Vector3(0f, 0f, height / 2f)
}

fun nodeFloorCenter(node: Node) = node.position + Vector3(0f, 0f, -wallHeight / 2f)

fun alignWithNodeFloor(meshInfo: MeshInfoMap, node: Node, mesh: MeshName) =
    nodeFloorCenter(node) + alignWithFloor(meshInfo)(mesh)

fun randomShift(dice: Dice) = dice.getFloat(-0.04f, 0.04f)

fun newWall(config: GenerationConfig, mesh: MeshName, dice: Dice, node: Node, position: Vector3, angleZ: Float): Hand {
  val biome = config.biomes[node.biome!!]!!
  val randomHorizontalFlip = getHorizontalFlip(dice, config.meshes[mesh.toString()]!!)
  val orientation = Quaternion().rotateZ(angleZ + randomHorizontalFlip)
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = true),
      meshes = config.meshes,
      mesh = mesh,
      position = position + floorOffset + alignWithFloor(config.meshes)(mesh) + Vector3(randomShift(dice), randomShift(dice), randomShift(dice)),
      scale = Vector3.unit,
      orientation = orientation,
      node = node.id,
      texture = biome.wallTexture
  )
}

fun segmentArcLength(segmentLength: Float, radius: Float): Float {
  val mod = radius * 2f
  return atan(segmentLength / mod) * mod
}

fun getSegmentAngleLength(segmentLength: Float, radius: Float): Float {
  return atan(segmentLength / radius / 2f) * 2f
}

fun wallPlacementFilter(dice: Dice, biome: BiomeInfo) =
    when {
      biome.attributes.contains(BiomeAttribute.wallsAll) -> true
      biome.attributes.contains(BiomeAttribute.wallsSome) -> dice.getFloat() < 0.75f
      else -> false
    }

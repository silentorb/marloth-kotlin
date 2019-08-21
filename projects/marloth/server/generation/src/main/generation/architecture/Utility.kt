package generation.architecture

import generation.misc.biomeInfoMap
import generation.structure.wallHeight
import mythic.ent.Id
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.physics.Body
import randomly.Dice
import scenery.enums.MeshId
import scenery.enums.TextureId
import simulation.entities.ArchitectureElement
import simulation.physics.CollisionObject
import simulation.main.Hand
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.misc.MeshInfoMap
import simulation.misc.Node

fun getHorizontalFlip(dice: Dice, wallData: WallData): Float =
    if (wallData.canFlipHorizontally && dice.getBoolean()) Pi else 0f

val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

fun newArchitectureMesh(meshInfo: MeshInfoMap, mesh: MeshId, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        node: Id,
                        architecture: ArchitectureElement,
                        texture: TextureId? = null, scale: Vector3 = Vector3.unit): Hand {
  val shape = meshInfo[mesh.name]!!
  return Hand(
      architecture = architecture,
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh.name,
          texture = texture?.name
      ),
      body = Body(
          position = position,
          orientation = orientation,
          nearestNode = node,
          scale = scale
      ),
      collisionShape = CollisionObject(shape = shape)
  )
}

fun alignWithCeiling(meshInfo: MeshInfoMap) = { meshId: MeshId ->
  val height = meshInfo[meshId.name]!!.height
  Vector3(0f, 0f, -height / 2f)
}

fun alignWithFloor(meshInfo: MeshInfoMap) = { meshId: MeshId ->
  val height = meshInfo[meshId.name]!!.height
  Vector3(0f, 0f, height / 2f)
}

fun nodeFloorCenter(node: Node) = node.position + Vector3(0f, 0f, -wallHeight / 2f)

fun alignWithNodeFloor(meshInfo: MeshInfoMap, node: Node, meshId: MeshId) =
    nodeFloorCenter(node) + alignWithFloor(meshInfo)(meshId)

fun randomShift(dice: Dice) = dice.getFloat(-0.04f, 0.04f)

fun newWall(meshInfo: MeshInfoMap, mesh: MeshId, dice: Dice, node: Node, position: Vector3, angleZ: Float): Hand {
  val biome = biomeInfoMap[node.biome]!!
  val wallData = wallDataMap[mesh]!!
  val randomHorizontalFlip = getHorizontalFlip(dice, wallData)
  val orientation = Quaternion().rotateZ(angleZ + randomHorizontalFlip)
  return newArchitectureMesh(
      architecture = ArchitectureElement(isWall = true),
      meshInfo = meshInfo,
      mesh = mesh,
      position = position + floorOffset + alignWithFloor(meshInfo)(mesh) + Vector3(randomShift(dice), randomShift(dice), randomShift(dice)),
      scale = Vector3.unit,
      orientation = orientation,
      node = node.id,
      texture = biome.wallTexture
  )
}

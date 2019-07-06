package generation.architecture

import generation.biomeInfoMap
import generation.structure.wallHeight
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.physics.Body
import simulation.physics.voidNodeId
import randomly.Dice
import marloth.definition.MeshId
import scenery.Shape
import marloth.definition.TextureId
import simulation.physics.CollisionObject
import scenery.MeshName
import simulation.main.Hand
import simulation.misc.Depiction
import simulation.misc.DepictionType
import simulation.misc.Node

fun getHorizontalFlip(dice: Dice, wallData: WallData): Float =
    if (wallData.canFlipHorizontally && dice.getBoolean()) Pi else 0f

val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

typealias MeshInfoMap = Map<MeshName, Shape>

fun newArchitectureMesh(meshInfo: MeshInfoMap, mesh: MeshId, position: Vector3, scale: Vector3 = Vector3.unit,
                        orientation: Quaternion = Quaternion(),
                        texture: TextureId? = null): Hand {
  val shape = meshInfo[mesh.name]!!
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh.name,
          texture = texture?.name
      ),
      body = Body(
          position = position,
          orientation = orientation,
          node = voidNodeId,
          scale = scale
      ),
      collisionShape = CollisionObject(shape = shape)
  )
}

fun newWall(meshInfo: MeshInfoMap, dice: Dice, node: Node, position: Vector3, angleZ: Float): Hand {
  val biome = biomeInfoMap[node.biome]!!
  val mesh = dice.getItem(biome.wallMeshes)
  val wallData = wallDataMap[mesh]!!
  val randomHorizontalFlip = getHorizontalFlip(dice, wallData)
  val orientation = Quaternion().rotateZ(angleZ + randomHorizontalFlip)
  return newArchitectureMesh(
      meshInfo = meshInfo,
      mesh = mesh,
      position = position + floorOffset + alignWithFloor(meshInfo)(mesh),
      scale = Vector3.unit,
      orientation = orientation,
      texture = biome.wallTexture
  )
}

package generation.architecture

import generation.structure.wallHeight
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.projectVector3
import physics.Body
import physics.voidNodeId
import randomly.Dice
import scenery.MeshId
import scenery.Shape
import scenery.TextureId
import simulation.*

fun getHorizontalFlip(dice: Dice, wallData: WallData): Float =
    if (wallData.canFlipHorizontally && dice.getBoolean()) Pi else 0f

val floorOffset = Vector3(0f, 0f, -wallHeight / 2f)

typealias MeshInfoMap = Map<MeshId, Shape>

fun newArchitectureMesh(meshInfo: MeshInfoMap, mesh: MeshId, position: Vector3, scale: Vector3 = Vector3.unit,
                        orientation: Quaternion = Quaternion(),
                        texture: TextureId? = null): Hand {
  val shape = meshInfo[mesh]!!
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = texture
      ),
      body = Body(
          position = position,
          orientation = orientation,
          node = voidNodeId,
          scale = scale
      ),
      collisionShape = shape
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

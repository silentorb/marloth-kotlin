package generation.architecture.old

import generation.general.*
import marloth.scenery.enums.*
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Hand
import simulation.misc.Node
import simulation.physics.CollisionGroups
import kotlin.math.atan

const val roundedMeshPadding = 0.08f

fun getHorizontalFlip(dice: Dice, info: ArchitectureMeshInfo): Float =
    if (info.attributes.contains(MeshAttribute.canFlipHorizontally) && dice.getBoolean()) Pi else 0f

//val ceilingOffset = Vector3(0f, 0f, wallHeight / 2f)
val ceilingOffset = Vector3(0f, 0f, wallHeight)
val floorOffsetOld = Vector3(0f, 0f, -wallHeight / 2f)

fun newArchitectureMesh(meshes: MeshInfoMap, mesh: MeshName, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        node: Id = 0L,
                        texture: TextureName? = null,
                        scale: Vector3 = Vector3.unit): Hand {
  val meshInfo = meshes[mesh]!!
  val shape = meshInfo.shape
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = mesh,
          texture = texture
      ),
      body = Body(
          position = position,
          orientation = orientation,
          nearestNode = node,
          scale = scale
      ),
      collisionShape = if (shape != null)
        CollisionObject(
            shape = shape,
            groups = CollisionGroups.static or CollisionGroups.affectsCamera or CollisionGroups.walkable,
            mask = CollisionGroups.staticMask
        )
      else
        null
  )
}

data class CommonArchitectConfig(
    val meshAttributes: MeshAttributes,
    val textureGroup: TextureGroup,
    val offset: Vector3,
    val aligner: VerticalAligner,
    val orientation: Quaternion? = null
)

typealias VerticalAligner = (Float) -> (Float)

val alignWithCeiling: VerticalAligner = { height -> -height / 2f }
val alignWithFloor: VerticalAligner = { height -> height / 2f }

fun align(meshInfo: MeshInfoMap, aligner: VerticalAligner) = { mesh: MeshName ->
  val height = meshInfo[mesh]!!.shape!!.height
  Vector3(0f, 0f, aligner(height))
}

//fun align(meshInfo: MeshInfoMap) = { mesh: MeshName ->
//  val height = meshInfo[mesh]!!.shape.height
//  Vector3(0f, 0f, height / 2f)
//}

fun nodeFloorCenter(node: Node) = node.position + Vector3(0f, 0f, -wallHeight / 2f)

fun alignWithNodeFloor(meshInfo: MeshInfoMap, node: Node, mesh: MeshName) =
    nodeFloorCenter(node) + align(meshInfo, alignWithFloor)(mesh)

fun randomShift(dice: Dice) = dice.getFloat(-0.04f, 0.04f)

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

//fun randomlySelectMesh(dice: Dice, meshes: MeshInfoMap, biome: BiomeInfo, attributes: MeshAttributes): MeshName {
//  val meshPool = filterMeshes(meshes, biome, attributes, QueryFilter.any)
//  return dice.takeOne(meshPool)
//}

fun applyTurnsOld(turns: Int): Float =
    (turns.toFloat() - 1) * Pi * 0.5f

fun applyTurns(turns: Int): Float =
    turns.toFloat() * Pi * 0.5f

fun getTurnDirection(turns: Int): Direction =
    when ((turns + 4) % 4) {
      0 -> Direction.east
      1 -> Direction.north
      2 -> Direction.west
      3 -> Direction.south
      else -> throw Error("Shouldn't be here")
    }

fun getTurnedSide(sides: Sides, turns: Int): Side? =
    sides[getTurnDirection(turns)]

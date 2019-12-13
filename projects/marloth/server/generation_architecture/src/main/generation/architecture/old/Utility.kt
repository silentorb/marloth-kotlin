package generation.architecture.old

import generation.architecture.definition.MeshAttribute
import generation.architecture.misc.*
import generation.general.BiomeAttribute
import generation.general.BiomeInfo
import generation.general.TextureGroup
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.physics.Body
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.MeshName
import marloth.scenery.enums.TextureId
import simulation.entities.ArchitectureElement
import silentorb.mythic.physics.CollisionObject
import simulation.main.Hand
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.misc.Node
import simulation.misc.cellLength
import kotlin.math.atan

const val roundedMeshPadding = 0.08f

val cellHalfLength = cellLength / 2f

fun getHorizontalFlip(dice: Dice, info: ArchitectureMeshInfo): Float =
    if (info.attributes.contains(MeshAttribute.canFlipHorizontally) && dice.getBoolean()) Pi else 0f

//val ceilingOffset = Vector3(0f, 0f, wallHeight / 2f)
val ceilingOffset = Vector3(0f, 0f, wallHeight)
val floorOffsetOld = Vector3(0f, 0f, -wallHeight / 2f)

fun newArchitectureMesh(meshes: MeshInfoMap, mesh: MeshName, position: Vector3,
                        orientation: Quaternion = Quaternion(),
                        architecture: ArchitectureElement,
                        node: Id = 0L,
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
      collisionShape = if (meshInfo.shape != null) CollisionObject(shape = meshInfo.shape) else null
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

//fun getStairTopFloorFacingAngle(graph: Graph, node: Node): Float {
//  val otherNode = getNonVerticalNeighbors(graph, node.id).first()
//  val otherNodePosition = graph.nodes[otherNode]!!.position
//  val vector = (otherNodePosition - node.position).normalize()
//  println("stair ${node.id} $otherNode $vector")
//  return getLookAtAngle(vector)
//}

fun applyTurns(turns: Int): Float =
    (turns.toFloat() - 1) * Pi * 0.5f

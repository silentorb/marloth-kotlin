package silentorb.marloth.world_generation

import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.spatial.Matrix
import simulation.main.Hand

data class HandInput(
    val meshes: MeshInfoMap,
)

typealias GetHand = (HandInput) -> Hand

data class SpatialNode(
    val transform: Matrix = Matrix.identity,
    val children: List<SpatialNode> = listOf(),
    val getHand: GetHand? = null
)

typealias SpatialNodeInput = String

typealias GetSpatialNode = (SpatialNodeInput) -> SpatialNode

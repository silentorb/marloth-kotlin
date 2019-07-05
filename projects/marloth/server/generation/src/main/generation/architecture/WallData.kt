package generation.architecture

import marloth.definition.MeshId

data class WallData(
    val canFlipHorizontally: Boolean = false,
    val canFlipUpsideDown: Boolean = false,
    val canRotateOnSide: Boolean = false // Not used yet
)

private val noRotate = WallData()
private val fullRotate = WallData(
    canFlipHorizontally = true,
    canFlipUpsideDown = true,
    canRotateOnSide = true
)

val wallDataMap: Map<MeshId, WallData> = mapOf(
    MeshId.squareWall to noRotate,
    MeshId.pillowWall to fullRotate
)

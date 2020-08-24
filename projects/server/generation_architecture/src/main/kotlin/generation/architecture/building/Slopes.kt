package generation.architecture.building

import generation.architecture.engine.*
import generation.architecture.matrical.mergeBuilders
import generation.architecture.matrical.quarterStep
import generation.general.Direction
import marloth.scenery.enums.MeshId
import silentorb.mythic.scenery.TextureName
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.misc.cellHalfLength
import simulation.misc.cellLength
import kotlin.math.asin

fun newSlopedFloorMesh(depiction: Depiction): Builder = { input ->
  val orientation = Quaternion()
  floorMeshBuilder(depiction, orientation = orientation)(input)
}

fun ledgeSlopeBuilder(texture: TextureName, ledgeTurns: Int) =
    mergeBuilders(
        newSlopedFloorMesh(Depiction(mesh = MeshId.fullSlope, texture = texture)),
        newSlopeEdgeBlock(Depiction(mesh = MeshId.largeBrick, texture = texture), quarterStep, ledgeTurns),
        roomWalls(Depiction(mesh = MeshId.wallSquareShort, texture = texture))
    )

fun newSlopeEdgeBlock(depiction: Depiction, height: Float, ledgeTurns: Int): Builder = { input ->
  val offset = Quaternion().rotateZ(applyTurnsOld(ledgeTurns))
      .transform(Vector3(0f, cellLength / 4f, 0f))
  val position = offset + Vector3(0f, -0.1f, height)
  floorMeshBuilder(depiction, offset = position)(input)
}

fun slopeBuilder(floor: Depiction, wall: Depiction) = mergeBuilders(
    newSlopedFloorMesh(floor),
    roomWalls(wall)
)

fun slopeWrapBuilder(wall: Depiction) =
    cubeWallsWithFeatures(
        listOf(WallFeature.window, WallFeature.lamp, WallFeature.none), lampOffset = Vector3(0f, 0f, -1.2f),
        possibleDirections = setOf(Direction.north, Direction.south),
        wallDepiction = wall
    )

fun cornerSlopeBuilder(texture: TextureName): Builder = { input ->
  val config = input.general.config
  val depiction = Depiction(
      mesh = MeshId.cornerSlope,
      texture = texture
  )
  listOf(
      newArchitectureMesh(
          meshes = config.meshes,
          depiction = depiction,
          position = Vector3(0f, 0f, -cellHalfLength)
      )
  )
}

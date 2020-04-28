package generation.architecture.building

import generation.architecture.old.*
import generation.general.TextureGroup
import generation.general.biomeTexture
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.misc.cellCenterOffset
import simulation.misc.cellLength
import simulation.misc.floorOffset

val curvedStaircases = blockBuilder() { input ->
  val biome = input.biome
  val config = input.general.config
  val mesh = MeshId.curvingStairStep.name
  val meshInfo = input.general.config.meshes[mesh]!!
  val stepWidth = meshInfo.shape!!.x
  val sweepLength = Pi * 2f

  val baseAngle = applyTurnsOld(input.turns)

  val stairHeight = cellLength
  val heightStep = 0.3f
  val stepCount = (stairHeight / heightStep).toInt()
  val angleStep = sweepLength / stepCount.toFloat()
  val baseOffset = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh)
  val columnRadius = 0.5f
  val roationVector = Vector3(columnRadius + stepWidth / 2f - 0.2f, 0f, 0f)
  (0 until stepCount).map { step ->
    val angle = baseAngle + step * angleStep
    val heightPosition = Vector3(0f, 0f, heightStep + step * heightStep)
    val rotationPosition = roationVector.transform(Matrix.identity.rotateZ(angle))
    val position = baseOffset + heightPosition + rotationPosition
    newArchitectureMesh(
        meshes = config.meshes,
        mesh = mesh,
        position = position,
        orientation = Quaternion().rotateZ(angle),
        texture = biomeTexture(biome, TextureGroup.floor)
    )
  }
      .plus(newArchitectureMesh(
          meshes = config.meshes,
          mesh = MeshId.spiralStaircaseColumn.name,
          position = input.position + cellCenterOffset,
          texture = biomeTexture(biome, TextureGroup.wall)
      ))
}

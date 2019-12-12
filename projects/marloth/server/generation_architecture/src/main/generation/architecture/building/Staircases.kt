package generation.architecture.building

import generation.architecture.old.*
import generation.general.TextureGroup
import generation.general.biomeTexture
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import simulation.entities.ArchitectureElement
import simulation.misc.cellLength

val curvedStaircases = blockBuilder() { input ->
  val biome = input.biome
  val config = input.config
  val mesh = MeshId.curvingStairStep.name
  val sweepLength = Pi * 2f

  val baseAngle = applyTurns(input.turns)

  val stairHeight = cellLength
  val heightStep = 0.3f
  val stepCount = (stairHeight / heightStep).toInt()
  val angleStep = sweepLength / stepCount.toFloat()
  val baseOffset = input.position + floorOffset + align(config.meshes, alignWithCeiling)(mesh)
  (0 until stepCount).map { step ->
    val angle = baseAngle + step * angleStep
    val heightPosition = Vector3(0f, 0f, heightStep + step * heightStep)
    val rotationPosition = Vector3(3.5f, 0f, 0f).transform(Matrix().rotateZ(angle))
    val position = baseOffset + heightPosition + rotationPosition
    newArchitectureMesh(
        architecture = ArchitectureElement(isWall = false),
        meshes = config.meshes,
        mesh = mesh,
        position = position,
        orientation = Quaternion().rotateZ(angle),
        texture = biomeTexture(biome, TextureGroup.floor)
    )
  }
}

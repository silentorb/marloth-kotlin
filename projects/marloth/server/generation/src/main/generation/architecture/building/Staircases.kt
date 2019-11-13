package generation.architecture.building

import generation.architecture.*
import generation.architecture.definition.floorOffset
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import generation.next.Builder
import generation.next.BuilderInput
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import scenery.enums.MeshId
import simulation.entities.ArchitectureElement
import simulation.main.Hand

val curvedStaircases: Builder = { input ->
  //val placeCurvedStaircases: Architect = { config, realm, dice ->
  val biome = input.biome
//  val mesh = randomlySelectMesh(dice, config.meshes, biome, setOf(MeshAttribute.placementStairStepCurved))
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

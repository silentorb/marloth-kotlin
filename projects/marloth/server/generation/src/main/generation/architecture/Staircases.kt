package generation.architecture

import generation.misc.MeshAttribute
import generation.misc.TextureGroup
import generation.misc.biomeTexture
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.entities.ArchitectureElement
import simulation.misc.ConnectionType
import simulation.misc.NodeAttribute

val placeCurvedStaircases: Architect = { config, realm, dice ->
  nodesWithAllAttributes(realm.graph, setOf(NodeAttribute.stairBottom))
      .flatMap { node ->
        val biome = config.biomes[node.biome]!!
        val mesh = randomlySelectMesh(dice, config.meshes, biome, setOf(MeshAttribute.placementStairStepCurved))
        val sweepLength = Pi * 1f
        val topNode = realm.graph.connections
            .first { it.contains(node.id) && it.type == ConnectionType.vertical }
            .other(node)

        val topNodeRecord = realm.graph.nodes[topNode]!!
        val topFloorOrientation = getStairTopFloorFacingAngle(realm.graph, topNodeRecord)
        val baseAngle = topFloorOrientation + Pi * 0.5f

        val stairHeight = topNodeRecord.position.z - node.position.z
        val heightStep = 0.3f
        val stepCount = (stairHeight / heightStep).toInt()
        val angleStep = sweepLength / stepCount.toFloat()
        val baseOffset = node.position + floorOffsetOld + align(config.meshes, alignWithCeiling)(mesh)
        (0 until stepCount).map { step ->
          val angle = baseAngle + step * angleStep
          val heightPosition = Vector3(0f, 0f, heightStep + step * heightStep)
          val rotationPosition = Vector3(5f, 0f, 0f).transform(Matrix().rotateZ(angle))
          val position = baseOffset + heightPosition + rotationPosition
          newArchitectureMesh(
              architecture = ArchitectureElement(isWall = false),
              meshes = config.meshes,
              mesh = mesh,
              position = position,
              orientation = Quaternion().rotateZ(angle),
              node = node.id,
              texture = biomeTexture(biome, TextureGroup.floor)
          )
        }
      }
}

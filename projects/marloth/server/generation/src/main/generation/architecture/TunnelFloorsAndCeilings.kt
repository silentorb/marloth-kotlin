package generation.architecture

import generation.misc.*
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.quarterAngle
import simulation.entities.ArchitectureElement
import simulation.misc.Node
import simulation.physics.old.getLookAtAngle

fun getTunnelFloorMesh(biome: BiomeInfo, info: TunnelInfo): Set<MeshAttribute> {
  return if (Math.abs(info.vector.z) > 0.2f)
    setOf(MeshAttribute.placementStairStep)
  else
    setOf(MeshAttribute.placementTunnelFloor)
}

fun placeTunnelFloorOrCeiling(common: CommonArchitectConfig): (Node) -> Architect = { node ->
  { config, realm, dice ->
    // Temporary improvement while the tunnel floor is rounded and the room floor isn't
    val tempHeightBump = 0.05f
    val biome = config.biomes[node.biome]!!
    val info = getTunnelInfo(realm.graph, node.id)
    val mesh = randomlySelectMesh(dice, config.meshes, biome, common.meshAttributes)
    val segmentLength = config.meshes[mesh]!!.shape.y
    val orientation = Quaternion()
        .rotateZ(getLookAtAngle(info.vector) + quarterAngle)
    val series = newFlushSeries(info.length, segmentLength)
    val verticalOffset = Vector3(0f, 0f, -0.05f)
    series.flushItems.map { offset ->
      newArchitectureMesh(
          architecture = ArchitectureElement(isWall = false),
          meshes = config.meshes,
          mesh = mesh,
          position = info.start + info.vector * offset + common.offset + align(config.meshes, common.aligner)(mesh) + verticalOffset,
          orientation = orientation,
          node = node.id,
          texture = biomeTexture(biome, common.textureGroup)
      )
    }
//    createOverlappingSeries(info.length, segmentLength, -0f) { step, stepOffset ->
////      val minorOffset = 0.001f
////      val minorMod = if (step % 2 == 0) -minorOffset else minorOffset
////      val minor = Vector3(0f, 0f, minorMod + tempHeightBump)
//      newArchitectureMesh(
//          architecture = ArchitectureElement(isWall = false),
//          meshes = config.meshes,
//          mesh = mesh,
//          position = info.start + info.vector * stepOffset + common.offset + align(config.meshes, common.aligner)(mesh),
//          orientation = orientation,
//          node = node.id,
//          texture = biomeTexture(biome, common.textureGroup)
//      )
//    }
  }
}

val placeTunnelFloors: Architect = { config, realm, dice ->
  tunnelNodes(realm.graph)
      .flatMap { node ->
        val biome = config.biomes[node.biome]!!
        val info = getTunnelInfo(realm.graph, node.id)
        val meshFilter = getTunnelFloorMesh(biome, info)
        val common = CommonArchitectConfig(meshFilter, TextureGroup.floor, floorOffsetOld, alignWithCeiling)
        placeTunnelFloorOrCeiling(common)(node)(config, realm, dice)
      }
}

val placeTunnelCeilings: Architect = { config, realm, dice ->
  tunnelNodes(realm.graph)
      .flatMap { node ->
        val biome = config.biomes[node.biome]!!
        val info = getTunnelInfo(realm.graph, node.id)
        val meshFilter = getTunnelFloorMesh(biome, info)
        val common = CommonArchitectConfig(meshFilter, TextureGroup.ceiling, ceilingOffset, alignWithFloor)
        placeTunnelFloorOrCeiling(common)(node)(config, realm, dice)
      }
}

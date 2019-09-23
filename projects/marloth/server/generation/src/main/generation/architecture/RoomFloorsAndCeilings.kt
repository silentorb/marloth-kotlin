package generation.architecture

import generation.misc.*
import mythic.spatial.Quaternion
import simulation.entities.ArchitectureElement
import simulation.misc.Node

fun placeRoomFloorOrCeiling(common: CommonArchitectConfig): (Node) -> HandArchitect = { node ->
  { config, realm, dice ->
    val biome = config.biomes[node.biome!!]!!

    val meshOptions = queryMeshes(config.meshes, biome.meshes, common.meshAttributes, QueryFilter.any)
    val mesh = dice.takeOne(meshOptions)
    newArchitectureMesh(
        architecture = ArchitectureElement(isWall = false),
        meshes = config.meshes,
        mesh = mesh,
        position = node.position + common.offset + align(config.meshes, common.aligner)(mesh),
        orientation = Quaternion(),
        node = node.id,
        texture = biomeTexture(biome, common.textureGroup)
    )
  }
}

val placeRoomFloors: Architect = { config, realm, dice ->
  roomNodes(realm.graph)
      .map { node ->
        val position = realm.cellMap[node.id]!!
        val isEmptyBelow = (1..6).none {
          realm.grid.cells.containsKey(position.copy(z = position.z - it))
        }

        val meshFilter = if (isEmptyBelow)
          setOf(MeshAttribute.placementShortRoomFloor, MeshAttribute.placementTallRoomFloor)
        else
          setOf(MeshAttribute.placementShortRoomFloor)

        val common = CommonArchitectConfig(meshFilter, TextureGroup.floor, floorOffset, alignWithCeiling)
        placeRoomFloorOrCeiling(common)(node)(config, realm, dice)
      }
}

private val ceilingMeshAttributes = setOf(MeshAttribute.placementRoomCeiling)

val placeRoomCeilings: Architect = { config, realm, dice ->
  val common = CommonArchitectConfig(ceilingMeshAttributes, TextureGroup.ceiling, ceilingOffset, alignWithFloor)
  roomNodes(realm.graph)
      .map { node ->
        placeRoomFloorOrCeiling(common)(node)(config, realm, dice)
      }
}

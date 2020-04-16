package generation.architecture.old

import silentorb.mythic.spatial.Vector3i
import simulation.misc.MapGrid

//fun placeRoomFloorOrCeiling(common: CommonArchitectConfig): (Node) -> HandArchitect = { node ->
//  { config, realm, dice ->
//    val biome = config.biomes[node.biome!!]!!
//
//    val meshOptions = filterMeshes(config.meshes, biome.meshes, QueryFilter.any)(common.meshAttributes)
//    val mesh = dice.takeOne(meshOptions)
//    newArchitectureMesh(
//        architecture = ArchitectureElement(isWall = false),
//        meshes = config.meshes,
//        mesh = mesh,
//        position = node.position + common.offset + align(config.meshes, common.aligner)(mesh),
//        orientation = common.orientation ?: Quaternion(),
//        node = node.id,
//        texture = biomeTexture(biome, common.textureGroup)
//    )
//  }
//}

fun isEmptyBelow(grid: MapGrid, position: Vector3i): Boolean =
    (1..6).none {
      grid.cells.containsKey(position.copy(z = position.z - it))
    }
/*
val placeRoomFloors: Architect = { config, realm, dice ->
  roomNodes(realm.graph)
      .map { node ->
        val position = realm.cellMap[node.id]!!
        val isStairTop = node.attributes.contains(NodeAttribute.stairTop)
        val meshFilter = when {
          isStairTop -> setOf(MeshAttribute.placementStairTopFloor)

          isEmptyBelow(realm.grid, position) -> setOf(
              MeshAttribute.placementShortRoomFloor,
              MeshAttribute.placementTallRoomFloor
          )

          else -> setOf(MeshAttribute.placementShortRoomFloor)
        }
        val orientation = if (isStairTop)
          Quaternion().rotateZ(getStairTopFloorFacingAngle(realm.graph, node))
        else
          null

        val common = CommonArchitectConfig(meshFilter, TextureGroup.floor, floorOffsetOld, alignWithCeiling, orientation)
        placeRoomFloorOrCeiling(common)(node)(config, realm, dice)
      }
}
*/

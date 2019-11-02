package generation.next

import generation.architecture.align
import generation.architecture.alignWithCeiling
import generation.architecture.newArchitectureMesh
import generation.elements.Polyomino
import generation.elements.Side
import generation.elements.enumeratePolyominoes
import generation.elements.newBlock
import generation.misc.*
import mythic.spatial.Vector3i
import simulation.entities.ArchitectureElement

private val wall: Side = setOf()
private val doorway: Side = setOf()

class Blocks {
  companion object {

    val singleCellRoom = newBlock(
        top = wall,
        bottom = wall,
        east = doorway,
        north = doorway,
        west = doorway,
        south = doorway
    )

  }
}

class PolyominoeDefinitions {
  companion object {

    val singleCellRoom: Polyomino = mapOf(
        Vector3i.zero to Blocks.singleCellRoom
    )

  }
}

class BuilderDefinitions {
  companion object {

    val singleCellRoom: Builder = { input ->
      val config = input.config
      val dice = input.dice
      val biome = config.biomes.values.first()
      val meshOptions = filterMeshes(config.meshes, biome.meshes, QueryFilter.any)(setOf(MeshAttribute.placementShortRoomFloor))
      val mesh = dice.takeOne(meshOptions)
      listOf(
          newArchitectureMesh(
              architecture = ArchitectureElement(isWall = false),
              meshes = config.meshes,
              mesh = mesh,
              position = input.position + align(config.meshes, alignWithCeiling)(mesh),
              texture = biomeTexture(biome, TextureGroup.floor)
          )
      )
    }

  }
}

fun allPolyominoes() = enumeratePolyominoes(PolyominoeDefinitions).toSet()

fun newBuilders(): Map<Polyomino, Builder> =
    mapOf(
        PolyominoeDefinitions.singleCellRoom to BuilderDefinitions.singleCellRoom
    )

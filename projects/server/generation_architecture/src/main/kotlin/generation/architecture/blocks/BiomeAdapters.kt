package generation.architecture.blocks

import generation.architecture.building.singleCellRoomBuilder
import generation.architecture.connecting.Sides
import generation.architecture.connecting.levelSides
import generation.architecture.engine.squareOffsets
import generation.architecture.matrical.BlockBuilder
import generation.architecture.matrical.TieredBlock
import generation.architecture.matrical.newBiomeSide
import generation.architecture.matrical.sides
import generation.general.Block
import simulation.entities.Depiction
import simulation.misc.BiomeName
import simulation.misc.CellAttribute

fun biomeAdapterCube(
    firstBiome: BiomeName,
    secondBiome: BiomeName,
    attributes: Set<CellAttribute> = setOf()
): TieredBlock = { level ->
  val localSides = levelSides[level]
  val firstSide = newBiomeSide(firstBiome, localSides.open)
  val secondSide = newBiomeSide(secondBiome, localSides.open)

  Block(
      name = "$firstBiome-$secondBiome-adapterCube$level",
      sides = sides(
          east = firstSide,
          north = secondSide,
          west = firstSide,
          south = firstSide,
          up = Sides.headroomVertical
      ),
      attributes = setOf(CellAttribute.traversable) + attributes,
      slots = squareOffsets(2)
  )
}

fun biomeAdapterHall(
    firstBiome: BiomeName,
    secondBiome: BiomeName,
    floor: Depiction,
    wall: Depiction,
    attributes: Set<CellAttribute> = setOf()
): (Int) -> BlockBuilder = { level ->
  val localSides = levelSides[level]
  val firstSide = newBiomeSide(firstBiome, localSides.open)
  val secondSide = newBiomeSide(secondBiome, localSides.open)

  Block(
      name = "$firstBiome-$secondBiome-adapterHall$level",
      sides = sides(
          north = secondSide,
          south = firstSide
      ),
      attributes = setOf(CellAttribute.traversable) + attributes,
      slots = squareOffsets(2)
  ) to singleCellRoomBuilder(floor, wall)
}

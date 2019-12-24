package generation.architecture.misc

import generation.architecture.definition.ConnectTypeMeshQueryMap
import generation.architecture.definition.connectionTypesToMeshQueries
import generation.general.*
import silentorb.mythic.ent.Id
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid
import simulation.misc.absoluteCellPosition

data class BuilderInput(
    val config: GenerationConfig,
    val grid: MapGrid,
    val blockGrid: BlockGrid,
    val cellBiomes: CellBiomeMap,
    val biome: BiomeInfo,
    val dice: Dice,
    val turns: Int,
    val cell: Vector3i,
    val position: Vector3,
    val sides: Sides,
    val selectMesh: MeshSelector,
    val getUsableCellSide: UsableConnectionTypes,
    val connectionTypesToMeshQueries: ConnectTypeMeshQueryMap
)

typealias Builder = (BuilderInput) -> List<Hand>

fun buildArchitecture(generationConfig: GenerationConfig, dice: Dice,
                      gridSideMap: GridSideMap,
                      workbench: Workbench,
                      blockMap: BlockMap,
                      cellBiomes: CellBiomeMap,
                      builders: Map<Block, Builder>): Map<Vector3i, List<Hand>> {

  val getUsableCellSide = getUsableCellSide(gridSideMap)
  val meshSource = newMeshSource(generationConfig.meshes)
  val selectMesh = randomlySelectMesh(dice, meshSource)

  return workbench.blockGrid.mapValues { (position, block) ->
    val info = blockMap[block]!!
    val biomeName = cellBiomes[position]!!
    val builder = builders[info.original]
    if (builder == null)
      throw Error("Could not find builder for block")

    val input = BuilderInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        position = absoluteCellPosition(position),
        turns = info.turns,
        grid = workbench.mapGrid,
        blockGrid = workbench.blockGrid,
        cell = position,
        biome = generationConfig.biomes[biomeName]!!,
        sides = allDirections.associateWith(getUsableCellSide(position)),
        selectMesh = selectMesh,
        getUsableCellSide = getUsableCellSide,
        connectionTypesToMeshQueries = connectionTypesToMeshQueries()
    )
    builder(input)
  }
}

fun mapArchitectureCells(elementMap: Map<Vector3i, List<IdHand>>): Map<Id, Vector3i> =
    elementMap
        .flatMap { (key, value) -> value.map { Pair(it.id, key) } }
        .associate { it }

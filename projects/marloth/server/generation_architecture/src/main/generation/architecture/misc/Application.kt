package generation.architecture.misc

import generation.abstracted.HorrorVacuiConfig
import generation.abstracted.horrorVacui
import generation.abstracted.newWindingWorkbench
import generation.abstracted.windingPath
import generation.architecture.definition.BlockDefinitions
import generation.architecture.definition.ConnectTypeMeshQueryMap
import generation.architecture.definition.connectionTypesToMeshQueries
import generation.general.*
import silentorb.mythic.ent.pipe
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.main.Hand
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

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
                      builders: Map<Block, Builder>): List<Hand> {

  val getUsableCellSide = getUsableCellSide(gridSideMap)
  val meshSource = newMeshSource(generationConfig.meshes)
  val selectMesh = randomlySelectMesh(dice, meshSource)

  return workbench.blockGrid.flatMap { (position, block) ->
    val info = blockMap[block]!!
    val biomeName = cellBiomes[position]!!
    val builder = builders[info.original]
    if (builder == null)
      throw Error("Could not find builder for block")

    val input = BuilderInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        position = applyCellPosition(position),
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

fun newWorkbench(dice: Dice, blocks: Set<Block>, independentConnections: Set<Any>, openConnectionTypes: Set<Any>,
                 roomCount: Int): Workbench {
  val blockConfig = BlockConfig(
      blocks = blocks,
      independentConnections = independentConnections,
      openConnections = openConnectionTypes
  )
  val firstBlockVariable = System.getenv("FIRST_BLOCK")
  val firstBlock = if (firstBlockVariable != null)
    getMember(BlockDefinitions, firstBlockVariable as String)
  else
    BlockDefinitions.singleCellDoorwayRoom

  return pipe(
      windingPath(dice, blockConfig, roomCount),
      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..2))
  )(newWindingWorkbench(firstBlock.block))
}

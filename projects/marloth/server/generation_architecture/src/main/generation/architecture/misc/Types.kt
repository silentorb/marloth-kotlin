package generation.architecture.misc

import generation.architecture.definition.ConnectTypeMeshQueryMap
import generation.architecture.definition.connectionTypesToMeshQueries
import generation.general.*
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

data class ArchitectureInput(
    val config: GenerationConfig,
    val grid: MapGrid,
    val blockGrid: BlockGrid,
    val cellBiomes: CellBiomeMap,
    val dice: Dice,
    val selectMesh: MeshSelector,
    val gridSideMap: GridSideMap,
    val connectionTypesToMeshQueries: ConnectTypeMeshQueryMap
)

fun newArchitectureInput(generationConfig: GenerationConfig, dice: Dice,
                         gridSideMap: GridSideMap,
                         workbench: Workbench,
                         cellBiomes: CellBiomeMap) =
    ArchitectureInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        grid = workbench.mapGrid,
        blockGrid = workbench.blockGrid,
        selectMesh = randomlySelectMesh(dice, newMeshSource(generationConfig.meshes)),
        gridSideMap = gridSideMap,
        connectionTypesToMeshQueries = connectionTypesToMeshQueries()
    )

data class BuilderInput(
    val general: ArchitectureInput,
    val biome: BiomeInfo,
    val turns: Int,
    val cell: Vector3i,
    val position: Vector3,
    val sides: Sides,
    val boundaryHands: Map<Direction, List<Hand>>
)

typealias Builder = (BuilderInput) -> List<Hand>

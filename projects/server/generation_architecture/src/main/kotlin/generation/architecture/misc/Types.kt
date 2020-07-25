package generation.architecture.misc

import generation.general.*
import silentorb.mythic.randomly.Dice
import simulation.main.Hand
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

data class ArchitectureInput(
    val config: GenerationConfig,
    val grid: MapGrid,
    val blockGrid: BlockGrid,
    val cellBiomes: CellBiomeMap,
    val dice: Dice,
    val selectMesh: MeshSelector
)

fun newArchitectureInput(generationConfig: GenerationConfig, dice: Dice,
                         workbench: Workbench,
                         cellBiomes: CellBiomeMap) =
    ArchitectureInput(
        config = generationConfig,
        grid = workbench.mapGrid,
        blockGrid = workbench.blockGrid,
        cellBiomes = cellBiomes,
        dice = dice,
        selectMesh = randomlySelectMesh(dice, newMeshSource(generationConfig.meshes))
    )

data class BuilderInput(
    val general: ArchitectureInput,
    val biome: BiomeInfo,
    val isNeighborPopulated: Map<Direction, Boolean>
)

typealias Builder = (BuilderInput) -> List<Hand>

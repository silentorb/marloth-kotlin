package generation.architecture.engine

import generation.general.*
import silentorb.mythic.randomly.Dice
import simulation.main.Hand

data class ArchitectureInput(
    val config: GenerationConfig,
    val blockGrid: BlockGrid,
    val dice: Dice,
    val selectMesh: MeshSelector
)

fun newArchitectureInput(generationConfig: GenerationConfig, dice: Dice,
                         blockGrid: BlockGrid) =
    ArchitectureInput(
        config = generationConfig,
        blockGrid = blockGrid,
        dice = dice,
        selectMesh = randomlySelectMesh(dice, newMeshSource(generationConfig.meshes))
    )

data class BuilderInput(
    val general: ArchitectureInput,
    val neighbors: Set<Direction>
)

typealias Builder = (BuilderInput) -> List<Hand>

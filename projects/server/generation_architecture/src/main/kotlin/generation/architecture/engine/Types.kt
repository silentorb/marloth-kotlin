package generation.architecture.engine

import generation.general.*
import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.main.Hand
import simulation.misc.Definitions

data class BlockElement(
    val target: String,
    val location: Vector3,
    val orientation: Quaternion,
    val scale: Vector3
)

typealias BlockElementsMap = Map<String, List<BlockElement>>

data class GenerationConfig(
    val definitions: Definitions,
    val blocks: BlockElementsMap,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)

data class ArchitectureInput(
    val config: GenerationConfig,
    val blockGrid: BlockGrid,
    val dice: Dice
)

fun newArchitectureInput(generationConfig: GenerationConfig, dice: Dice,
                         blockGrid: BlockGrid) =
    ArchitectureInput(
        config = generationConfig,
        blockGrid = blockGrid,
        dice = dice
    )

data class BuilderInput(
    val general: ArchitectureInput,
    val neighbors: Set<Direction>
)

typealias Builder = (BuilderInput) -> List<Hand>

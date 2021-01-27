package generation.architecture.engine

import generation.general.*
import marloth.scenery.enums.MeshInfoMap
import marloth.scenery.enums.MeshShapeMap
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.misc.CellAttribute
import simulation.misc.Definitions

data class BlockElement(
    val target: String,
    val location: Vector3,
    val orientation: Quaternion,
    val scale: Vector3
)

data class ImportedAttributes(
    val cell: Vector3i,
    val attributes: List<CellAttribute>
)

data class Polyomino(
    val attributes: List<ImportedAttributes>,
    val cells: List<Vector3i>,
    val elements: List<BlockElement>
)

typealias PolyominoMap = Map<String, Polyomino>

data class GenerationConfig(
    val definitions: Definitions,
    val polyominoes: PolyominoMap,
    val meshes: MeshInfoMap,
    val meshShapes: MeshShapeMap,
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

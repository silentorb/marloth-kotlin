package generation.architecture.engine

import generation.general.BlockGrid
import generation.general.CellDirection
import generation.general.Direction
import marloth.scenery.enums.MeshInfoMap
import marloth.scenery.enums.MeshShapeMap
import silentorb.mythic.ent.GraphLibrary
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
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
    val seed: Long,
    val definitions: Definitions,
    val polyominoes: PolyominoMap,
    val meshes: MeshInfoMap,
    val meshShapes: MeshShapeMap,
    val includeEnemies: Boolean,
    val roomCount: Int,
    val graphLibrary: GraphLibrary,
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
    val neighborOld: Set<Direction>,
    val neighbors: Map<CellDirection, String>
)

typealias Builder = (BuilderInput) -> Any

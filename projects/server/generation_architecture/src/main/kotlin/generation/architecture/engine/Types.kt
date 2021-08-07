package generation.architecture.engine

import generation.general.Block
import generation.general.BlockGrid
import generation.general.CellDirection
import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.scenery.ExpansionLibrary
import silentorb.mythic.lookinglass.ResourceInfo
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import simulation.main.Hand
import simulation.main.NewHand
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
    val meshes: MeshInfoMap,
    val resourceInfo: ResourceInfo,
    val includeEnemies: Boolean,
    val cellCount: Int,
    val expansionLibrary: ExpansionLibrary,
    val hands: Table<NewHand> = mapOf(),
    val propGroups: Map<String, Set<String>>,
    val propGraphs: Map<String, Graph>,
    val level: Int = 1,
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
    val neighbors: Map<CellDirection, String>,
    val turns: Int,
    val height: Int,
)

typealias Builder = (BuilderInput) -> Any

typealias BlockBuilder = Pair<Block, Builder>

fun mergeBuilders(vararg builders: Builder): Builder {
    return { input ->
        builders.flatMap { it(input) as List<Hand> }
    }
}

operator fun Builder.plus(builder: Builder): Builder =
    mergeBuilders(this, builder)

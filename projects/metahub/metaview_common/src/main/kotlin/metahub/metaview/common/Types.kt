package metahub.metaview.common

import metahub.core.Graph
import metahub.core.Port
import mythic.ent.Id

typealias CommonTransform = (CommonState) -> CommonState

data class GraphInteraction(
    val nodeSelection: List<Id> = listOf(),
    val portSelection: List<Port> = listOf(),
    val mode: GraphMode = GraphMode.normal
)

data class GuiState(
    val graphDirectory: String,
    val activeGraph: String? = null,
    val graphInteraction: GraphInteraction = GraphInteraction(),
    val previewFinal: Boolean = false
)

enum class GraphMode {
  connecting,
  normal
}

data class CommonState(
    val gui: GuiState,
    val graph: Graph? = null,
    val graphNames: List<String> = listOf()
)

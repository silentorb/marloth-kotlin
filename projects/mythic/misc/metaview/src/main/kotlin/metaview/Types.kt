package metaview

import metahub.Engine
import metahub.Graph
import metahub.Port
import mythic.ent.Id

data class GuiState(
    val projectPath: String,
    val activeGraph: String? = null,
    val graphInteraction: GraphInteraction = GraphInteraction(),
    val tilePreview: Boolean = false
)

data class GraphInteraction(
    val nodeSelection: List<Id> = listOf(),
    val portSelection: List<Port> = listOf(),
    val mode: GraphMode = GraphMode.normal
)

enum class GraphMode {
  connecting,
  normal
}

data class State(
    val gui: GuiState,
    val graph: Graph? = null,
    val graphNames: List<String>
)

data class Village(
    val engine: Engine
)

enum class StandardValueType {
  color,
  grayscale
}
package metaview

import metahub.core.Engine
import metahub.core.Graph
import metahub.core.Port
import metaview.texturing.TexturingState
import mythic.ent.Id

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

enum class Domain {
  modeling,
  texturing
}

data class State(
    val domain: Domain = Domain.texturing,
    val gui: GuiState,
    val otherDomains: Map<Domain, GuiState> = mapOf(),
    val graph: Graph? = null,
    val texturing: TexturingState = TexturingState(),
    val graphNames: List<String> = listOf()
)

data class ConfigState(
    val domain: Domain = Domain.texturing,
    val guis: Map<Domain, GuiState> = mapOf(),
    val texturing: TexturingState = TexturingState()
)

data class Village(
    val engine: Engine
)
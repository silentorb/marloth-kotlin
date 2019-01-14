package metaview

import metahub.Engine
import metahub.Graph
import metahub.Port
import mythic.ent.Id

data class Config(
    val projectPath: String
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
    val config: Config,
    val textureName: String? = null,
    val graphInteraction: GraphInteraction = GraphInteraction(),
    val graph: Graph? = null,
    val textures: List<String>
)

data class Village(
    val engine: Engine
)

enum class StandardValueType {
  color,
  grayscale
}
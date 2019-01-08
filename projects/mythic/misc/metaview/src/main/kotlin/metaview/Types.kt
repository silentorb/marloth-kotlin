package metaview

import metahub.Engine
import metahub.Graph

data class Config(
    val projectPath: String
)

data class State(
    val config: Config,
    val textureName: String? = null,
    val graph: Graph? = null
)

data class Village(
    val engine: Engine
)
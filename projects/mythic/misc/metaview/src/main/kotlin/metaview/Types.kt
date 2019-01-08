package metaview

import metahub.Engine
import metahub.Graph
import mythic.ent.Id

data class Config(
    val projectPath: String
)

data class State(
    val config: Config,
    val textureName: String? = null,
    val nodeSelection: List<Id> = listOf(),
    val graph: Graph? = null
)

data class Village(
    val engine: Engine
)
package lab.gui

import metahub.Engine
import metahub.Graph

data class State(
    val textureName: String? = null,
    val graph: Graph? = null
)

fun newState() =
    State()

data class Village(
    val engine: Engine
)
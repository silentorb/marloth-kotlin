package metahub.metaview.front

import metahub.core.Engine
import metahub.metaview.common.CommonState
import metahub.metaview.common.GuiState
import metahub.metaview.texturing.TexturingState

enum class Domain {
  modeling,
  texturing
}

data class ConfigState(
    val domain: Domain = Domain.texturing,
    val guis: Map<Domain, GuiState> = mapOf(),
    val texturing: TexturingState = TexturingState()
)

data class Village(
    val engine: Engine
)

data class AppState(
    val common: CommonState,
    val domain: Domain = Domain.texturing,
    val texturing: TexturingState = TexturingState(),
    val otherDomains: Map<Domain, GuiState> = mapOf()
)

enum class DomainEvent {
  switchDomain
}
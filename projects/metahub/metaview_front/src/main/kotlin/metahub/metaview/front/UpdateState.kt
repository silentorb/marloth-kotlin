package metahub.metaview.front

import metahub.core.Engine
import metahub.core.GraphTransform
import metahub.metaview.common.*
import metahub.metaview.texturing.TexturingEvent
import metahub.metaview.texturing.TexturingTransform
import metahub.metaview.texturing.updateTexturingState
import mythic.ent.pipe
import mythic.ent.transformIf

typealias AppTransform = (AppState) -> AppState

fun commonTransform(transform: CommonTransform): AppTransform = { state ->
  state.copy(
      common = transform(state.common)
  )
}

fun texturingTransform(transform: TexturingTransform): AppTransform = { state ->
  state.copy(
      texturing = transform(state.texturing)
  )
}

private fun changeDomain(domain: Domain): AppTransform = { state ->
  state.copy(
      domain = domain,
      common = state.common.copy(
          gui = state.otherDomains.getValue(domain)
      ),
      otherDomains = state.otherDomains.minus(domain).plus(state.domain to state.common.gui)
  )
}

fun selectDomain(engine: Engine, nodeDefinitions: NodeDefinitionMap, domain: Domain): AppTransform =
    transformIf({ it.domain != domain }, pipe(
        changeDomain(domain),
        commonTransform(pipe(loadGraphs, selectGraph(engine, nodeDefinitions)))
    ))

fun updateDomainState(engine: Engine, nodeDefinitions: NodeDefinitionMap, event: DomainEvent, data: Any) =
    when (event) {
      DomainEvent.switchDomain -> selectDomain(engine, nodeDefinitions, data as Domain)
    }

val onNewGraph: GraphTransform = { graph ->
  graph.copy(
      nodes = graph.nodes.plus(setOf(1L)),
      functions = graph.functions.plus(mapOf(1L to textureOutput))
  )
}

fun updateAppState(engine: Engine, nodeDefinitions: NodeDefinitionMap, commonUpdater: CommonStateUpdater, updateHistory: HistoryUpdater<AppState>, focus: FocusContext, event: Event): AppTransform {
  val eventType = event.type
  return when {
    eventType is DomainEvent -> updateDomainState(engine, nodeDefinitions, eventType, event.data)
    eventType is CommonEvent -> commonTransform(commonUpdater(focus, eventType, event.data))
    eventType is HistoryEvent -> updateHistory(eventType)
    eventType is TexturingEvent -> texturingTransform(updateTexturingState(eventType, event.data))
    else -> throw Error("Unsupported event type")
  }
}

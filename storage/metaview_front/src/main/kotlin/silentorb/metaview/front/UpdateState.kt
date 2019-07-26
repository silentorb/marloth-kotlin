package silentorb.metaview.front

import silentorb.metahub.core.Engine
import silentorb.metahub.core.GraphTransform
import silentorb.metaview.common.*
import silentorb.metaview.texturing.TexturingTransform
import silentorb.metaview.texturing.textureOutput
import mythic.ent.pass
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
        commonTransform(pipe(loadGraphs, refreshGraph(engine, nodeDefinitions)))
    ))

fun updateDomainState(engine: Engine, nodeDefinitions: NodeDefinitionMap, event: DomainEvent, data: Any) =
    when (event) {
      DomainEvent.switchDomain -> selectDomain(engine, nodeDefinitions, data as Domain)
    }

val onNewGraph: GraphTransform = { graph ->
  if (graph.nodes.none())
    graph.copy(
        nodes = graph.nodes.plus(setOf(1L)),
        functions = graph.functions.plus(mapOf(1L to textureOutput))
    )
  else
    graph
}

fun domainListener(engine: Engine, nodeDefinitions: NodeDefinitionMap): StateTransformListener<AppState> = { change ->
  val eventType = change.event.type
  if (eventType is DomainEvent)
    updateDomainState(engine, nodeDefinitions, eventType, change.event.data)
  else
    ::pass
}

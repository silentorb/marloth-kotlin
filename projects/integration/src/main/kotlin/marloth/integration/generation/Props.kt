package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.GraphLibrary
import silentorb.mythic.ent.scenery.*
import simulation.misc.distAttributes

typealias PropMap = Map<String, Set<String>>

fun categorizeProps(graphs: GraphLibrary): PropMap =
    graphs
        .mapValues { (name, graph) ->
          val roots = getGraphRoots(graph)
          roots
              .flatMap { node ->
                getNodeAttributes(graph, node).intersect(distAttributes)
              }
              .toSet()
        }
        .filter { it.value.any() }

fun preparePropGraphs(expansionLibrary: ExpansionLibrary, props: Collection<String>): Map<String, Graph> =
    props.associateWith { expandGraphInstances(expansionLibrary, expansionLibrary.graphs[it]!!) }

fun filterPropGraphs(config: GenerationConfig, attributes: Collection<String>) =
    config.propGroups
        .filter { it.value.containsAll(attributes) }
        .map { config.propGraphs[it.key]!! }

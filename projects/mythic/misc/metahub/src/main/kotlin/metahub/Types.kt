package metahub

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table

data class Node(
    override val id: Id,
    val function: String
) : Entity

data class PortReference(
    val node: Id,
    val port: String
)

data class Connection(
    override val id: Id,
    val ends: Pair<PortReference, PortReference>
) : Entity

data class Graph(
    val nodes: Table<Node>,
    val connections: Table<Connection>,
    val values: Map<Id, Any>
)

typealias Function = (Map<String, Any>) -> Map<String, Any>

data class Engine(
    val functions: Map<String, Function>
)

fun nextStage(nodes: Table<Node>, connections: Collection<Connection>): List<Id> {
  return nodes.filterKeys { node -> connections.none { it.ends.second.node == node } }
      .map { it.key }
}

fun arrangeGraphStages(graph: Graph): List<List<Id>> {
  var nodes = graph.nodes
  var connections = graph.connections.values
  var result = listOf<List<Id>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.ends.first.node) }
  }

  return result
}

typealias OutputValues = Map<PortReference, Any>

fun prepareArguments(graph: Graph, outputValues: OutputValues, nodeId: Id): Map<String, Any> {
  val outputPorts = graph.connections.filterValues { it.ends.first.node == nodeId }.map { it.value.ends.first }
      .distinct()

  return outputValues.filterKeys { outputPorts.contains(it) }
      .mapKeys { it.key.port }
}

fun executeStage(graph: Graph, engine: Engine): (OutputValues, List<Id>) -> OutputValues = { values, stage ->
  val newValues = stage.flatMap { id ->
    val node = graph.nodes[id]!!
    val function = engine.functions[node.function]
    if (function == null) {
      val value = graph.values[id]!!
      listOf(Pair(PortReference(id, "output"), value))
    } else {
      val arguments = prepareArguments(graph, values, id)
      val result = function(arguments)
      result.map { (key, value) ->
        val reference = PortReference(id, key)
        Pair(reference, value)
      }
    }
  }
      .associate { it }
  values.plus(newValues)
}

fun execute(graph: Graph, engine: Engine, stages: List<List<Id>>): OutputValues {
  return stages.fold(mapOf(), executeStage(graph, engine))
}
package metahub

import mythic.ent.Id

data class Connection(
    val input: Id,
    val output: Id,
    val port: String
)

data class Graph(
    val nodes: Set<Id> = setOf(),
    val functions: Map<Id, String> = mapOf(),
    val connections: List<Connection> = listOf(),
    val values: Map<Id, Map<String, Any>> = mapOf(),
    val outputs: Map<String, Id> = mapOf()
)

typealias Arguments = Map<String, Any>

typealias Function = (Map<String, Any>) -> Any

typealias TypeMapper = (Any) -> Any?

data class Engine(
    val functions: Map<String, Function>,
    val typeMappers: List<TypeMapper>
)

fun nextStage(nodes: Set<Id>, connections: Collection<Connection>): List<Id> {
  return nodes.filter { node -> connections.none { it.output == node } }
      .map { it }
}

fun arrangeGraphStages(graph: Graph): List<List<Id>> {
  var nodes = graph.nodes
  var connections = graph.connections
  var result = listOf<List<Id>>()

  while (nodes.any()) {
    val nextNodes = nextStage(nodes, connections)
    result = result.plusElement(nextNodes)
    nodes = nodes.minus(nextNodes)
    connections = connections.filter { !nextNodes.contains(it.input) }
  }

  return result
}

typealias OutputValues = Map<Id, Any>

fun prepareArguments(graph: Graph, outputValues: OutputValues, nodeId: Id): Map<String, Any> {
  val nodeValues = graph.values[nodeId]
  val values = if (nodeValues != null)
    nodeValues.map { Pair(it.key, it.value) }
  else
    listOf()

  return graph.connections
      .filter { it.output == nodeId }
      .map { Pair(it.port, outputValues[it.input]!!) }
      .plus(values)
      .associate { it }
}

fun executeNode(graph: Graph, engine: Engine, values: OutputValues, id: Id): Any {
  val functionName = graph.functions[id]
  val function = engine.functions[functionName]!!
  val arguments = prepareArguments(graph, values, id)
  return function(arguments)
}

fun executeStage(graph: Graph, engine: Engine): (OutputValues, List<Id>) -> OutputValues = { values, stage ->
  val newValues = stage.map { id ->
    Pair(id, executeNode(graph, engine, values, id))
  }
      .associate { it }
  values.plus(newValues)
}

fun execute(engine: Engine, graph: Graph, stages: List<List<Id>>): OutputValues {
  return stages.fold(mapOf(), executeStage(graph, engine))
}

fun execute(engine: Engine, graph: Graph): OutputValues {
  val stages = arrangeGraphStages(graph)
  return execute(engine, graph, stages)
}

fun getGraphOutput(graph: Graph, values: OutputValues): Map<String, Any> =
    graph.outputs.mapValues { values[it.value]!! }

fun executeAndFormat(engine: Engine, graph: Graph): Map<String, Any> {
  val values = execute(engine, graph)
  return getGraphOutput(graph, values)
}

fun mapValues(engine: Engine): (Graph) -> Graph = { graph ->
  val values = graph.values.mapValues { nodeValues ->
    nodeValues.value.mapValues { entry ->
      val newValue = engine.typeMappers.mapNotNull { it(entry.value) }.firstOrNull()
      if (newValue != null)
        newValue
      else
        entry
    }
  }

  graph.copy(values = values)
}
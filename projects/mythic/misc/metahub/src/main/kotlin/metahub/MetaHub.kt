package metahub

import mythic.ent.Id

data class Connection(
    val input: Id,
    val output: Id,
    val port: String
)

data class InputValue(
    val value: Any,
    val node: Id,
    val port: String
)

fun isSameInput(a: InputValue, b: InputValue): Boolean =
    a.node == b.node && a.port == b.port

//val isSameInput: (InputValue, InputValue) -> Boolean = { b ->
//  a.node == b.node && a.port == b.port
//}

data class Graph(
    val nodes: Set<Id> = setOf(),
    val functions: Map<Id, String> = mapOf(),
    val connections: List<Connection> = listOf(),
    val values: List<InputValue> = listOf(),
    val outputs: Map<String, Id> = mapOf()
)

typealias Arguments = Map<String, Any>

typealias Function = (Map<String, Any>) -> Any

typealias TypeMapper = (Any) -> Any?

data class InputDefinition(
    val type: String
)

data class NodeDefinition(
    val inputs: Map<String, InputDefinition>,
    val outputType: String
)

data class Engine(
    val functions: Map<String, Function>,
    val typeMappers: List<TypeMapper>
)

fun nextStage(nodes: Set<Id>, connections: Collection<Connection>): List<Id> {
  return nodes.filter { node -> connections.none { it.output == node } }
      .map { it }
}

fun getInputValue(graph: Graph): (Id, String) -> InputValue? = { node, port ->
  graph.values.firstOrNull { it.node == node && it.port == port }
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

fun mapValues(engine: Engine): (Graph) -> Graph = { graph ->
  val values = graph.values.map { entry ->
    val newValue = engine.typeMappers.mapNotNull { it(entry.value) }.firstOrNull()
    if (newValue != null)
      entry.copy(value = newValue)
    else
      entry
  }

  graph.copy(values = values)
}
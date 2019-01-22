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

data class Port(
    val node: Id,
    val input: String
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
    val values: List<InputValue> = listOf()
)

typealias GraphTransform = (Graph) -> Graph

typealias Arguments = Map<String, Any>

typealias Function = (Map<String, Any>) -> Any

typealias TypeMapper = (Any) -> Any?

typealias OutputTypes = Set<String>

data class Engine(
    val functions: Map<String, Function>,
    val typeMappers: List<TypeMapper>,
    val outputTypes: OutputTypes
)

fun nextStage(nodes: Set<Id>, connections: Collection<Connection>): List<Id> {
  return nodes.filter { node -> connections.none { it.output == node } }
      .map { it }
}

fun getInputValue(graph: Graph): (Id, String) -> InputValue? = { node, port ->
  graph.values.firstOrNull { it.node == node && it.port == port }
}

fun arrangeGraphStages(outputTypes: OutputTypes, graph: Graph): List<List<Id>> {
  var nodes = graph.nodes.filter { !outputTypes.contains(graph.functions[it]) }.toSet()
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

fun mapValues(engine: Engine): GraphTransform = { graph ->
  val values = graph.values.map { entry ->
    val newValue = engine.typeMappers.mapNotNull { it(entry.value) }.firstOrNull()
    if (newValue != null)
      entry.copy(value = newValue)
    else
      entry
  }

  graph.copy(values = values)
}

fun nextNodeId(graph: Graph): Id =
    (graph.nodes.sortedDescending().firstOrNull() ?: 0L) + 1L

fun newNode(function: String, defaultValues: Map<String, Any>, id: Id): GraphTransform = { graph ->
  val newValues = defaultValues.map { (key, value) ->
    InputValue(
        node = id,
        port = key,
        value = value
    )
  }
  graph.copy(
      nodes = graph.nodes.plus(id),
      functions = graph.functions.plus(Pair(id, function)),
      values = graph.values.plus(newValues)
  )
}

fun deleteNodes(ids: Collection<Id>): GraphTransform = { graph ->
  graph.copy(
      nodes = graph.nodes.minus(ids),
      connections = graph.connections.filter { !ids.contains(it.input) && !ids.contains(it.output) },
      values = graph.values.filter { !ids.contains(it.node) },
      functions = graph.functions.minus(ids)
  )
}

fun newConnection(input: Id, output: Id, port: String): GraphTransform = { graph ->
  val connection = Connection(
      input = input,
      output = output,
      port = port
  )
  graph.copy(
      connections = graph.connections.plus(connection)
  )
}

fun newConnection(node: Id, port: Port): GraphTransform = newConnection(node, port.node, port.input)

fun deleteConnections(ports: List<Port>): GraphTransform = { graph ->
  graph.copy(
      connections = graph.connections.filter { connection ->
        ports.none { it.node == connection.output && it.input == connection.port }
      }
  )
}

fun getConnection(graph: Graph, port: Port): Connection? =
    graph.connections.firstOrNull { it.output == port.node && it.port == port.input }

fun isOutputNode(outputTypes: Set<String>): (Graph, Id) -> Boolean = { graph, id ->
  outputTypes.contains(graph.functions[id])
}

fun setValue(node: Id, port: String, value: Any): GraphTransform = { graph ->
  val truncatedValues = graph.values.filter { it.node != node || it.port != port }
  val newValues = truncatedValues.plus(InputValue(
      value = value,
      node = node,
      port = port
  ))
  graph.copy(
      values = newValues
  )
}

fun <T> modifyValue(node: Id, port: String, transform: (T) -> T): GraphTransform = { graph ->
  val value = transform(graph.values.first { it.node == node && it.port == port }.value as T)
  setValue(node, port, value as Any)(graph)
}
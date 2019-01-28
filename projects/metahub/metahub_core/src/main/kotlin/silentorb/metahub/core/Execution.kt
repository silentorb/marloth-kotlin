package silentorb.metahub.core

import mythic.ent.Id

typealias OutputValues = Map<Id, Any>

fun prepareArguments(graph: Graph, outputValues: OutputValues, nodeId: Id): Map<String, Any> {
  val values = graph.values
      .filter { it.node == nodeId }
      .map { Pair(it.port, it.value) }

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
  val stages = arrangeGraphStages(engine.outputTypes, graph)
  return execute(engine, graph, stages)
}

fun getGraphOutput(outputTypes: OutputTypes, graph: Graph, values: OutputValues): Map<String, Any> {
  val outputNode = graph.functions.entries.first { outputTypes.contains(it.value) }
  return graph.connections.filter { it.output == outputNode.key }
      .associate { Pair(it.port, values[it.input]!!) }
}

fun executeAndFormat(engine: Engine, graph: Graph): Map<String, Any> {
  val values = execute(engine, graph)
  return getGraphOutput(engine.outputTypes, graph, values)
}
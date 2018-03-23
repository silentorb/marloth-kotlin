package mythic.sculpting

import org.joml.plus

fun edgeLoopNext(edge: FlexibleEdge) =
    edge.next!!.edges[0].next!!

fun getEdgeFaceLoop(edge: FlexibleEdge): List<FlexibleEdge> {
  val result = mutableListOf<FlexibleEdge>()
  var current = edge.next!!
  var i = 0
  do {
    result.add(current)
    current = current.next!!
    if (i++ > 20)
      throw Error("Possible infinite loop caused by an invalid edge loop.")

  } while (current != edge)

  return result
}

typealias EdgeExplorer = (FlexibleEdge) -> FlexibleEdge?

fun gatherEdges(explore: EdgeExplorer, edge: FlexibleEdge): List<FlexibleEdge> {
  val result = mutableListOf<FlexibleEdge>()
  var current = edge
  var i = 0
  do {
    result.add(current)
    val next = explore(current)
    if (next == null)
      break

    current = next
    if (i++ > 20)
      break

  } while (current != edge)

  return result
}

val edgeLoopNext: EdgeExplorer = { edge ->
  if (edge.next!!.edges.size == 0)
    null
  else
    edge.next!!.edges[0].next!!
}

val edgeLoopReversedNext: EdgeExplorer = { edge ->
  if (edge.previous!!.edges.size == 0)
    null
  else
    edge.previous!!.edges[0].previous!!
}

fun getEdgeLoop(edge: FlexibleEdge): List<FlexibleEdge> = gatherEdges(edgeLoopNext, edge)
fun getEdgeLoopReversed(edge: FlexibleEdge): List<FlexibleEdge> = gatherEdges(edgeLoopReversedNext, edge)

fun getCenter(edges: List<FlexibleEdge>) =
    edges.map { it.first }.reduce { a, b -> a + b } / edges.size.toFloat()
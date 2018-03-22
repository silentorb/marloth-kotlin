package mythic.sculpting

import org.joml.div
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

fun getEdgeLoop(edge: FlexibleEdge): List<FlexibleEdge> {
  val result = mutableListOf<FlexibleEdge>()
  var current = edge
  var i = 0
  do {
    result.add(current)
    if (current.next!!.edges.size == 0)
      break

    current = current.next!!.edges[0].next!!
    if (i++ > 20)
      break

  } while (current != edge)

  return result
}

fun getEdgeLoopReversed(edge: FlexibleEdge): List<FlexibleEdge> {
  val result = mutableListOf<FlexibleEdge>()
  var current = edge
  var i = 0
  do {
    result.add(current)
    if (current.previous!!.edges.size == 0)
      break

    current = current.previous!!.edges[0].previous!!
    if (i++ > 20)
      break

  } while (current != edge)

  return result
}

fun getCenter(edges: List<FlexibleEdge>) =
    edges.map { it.first }.reduce { a, b -> a + b } / edges.size.toFloat()
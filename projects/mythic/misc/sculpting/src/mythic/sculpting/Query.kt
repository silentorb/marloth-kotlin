package mythic.sculpting

fun edgeLoopNext(edge: FlexibleEdge) =
    edge.next!!.edges[0].next!!

fun getEdgeLoop(edge: FlexibleEdge): List<FlexibleEdge> {
  val result = mutableListOf<FlexibleEdge>()
  var current = edge.next!!
  do {
    result.add(current)
    current = edge.next!!
  } while (current != edge)

  return result
}
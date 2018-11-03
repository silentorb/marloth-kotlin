package generation.abstract

import mythic.ent.IdSource
import simulation.*

data class OldRealm(
    val graph: Graph,
    val nextId: IdSource
) {

  val nodes: List<Node>
    get() = graph.nodes
}

fun faceNodes(info: ConnectionFace) =
    listOf(info.firstNode, info.secondNode)

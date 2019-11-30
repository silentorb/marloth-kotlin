package generation.abstracted

import mythic.ent.Id
import simulation.main.Deck
import simulation.misc.Graph
import simulation.misc.Node

// Normally Body.nearestNode is used as a cached variable that can be reconstructed.
// But for the initial architecture pass the source of truth is reversed
// and Body.nearestNode is temporarily used as the source-of-truth for node radius calculation.
//fun calculateInitialNodeRadius(deck: Deck, node: Id, nodeRecord: Node): Float {
//  val nodePosition = nodeRecord.position
//  val bodies = deck.bodies
//      .filter { it.value.nearestNode == node }
//      .filter {
//        val architecture = deck.architecture[it.key]
//        architecture != null && architecture.isWall
//      }
//  if (bodies.none())
//    return 1f
//
//  return bodies
//      .entries.map { (key, body) ->
//    val shape = deck.collisionShapes[key]
//    if (shape == null)
//      0f
//    else {
////      val radiusScale = max(max(body.scale.x, body.scale.y), body.scale.z)
//      body.position.distance(nodePosition) // + shape.shape.radius * radiusScale
//    }
//  }
//      .firstSortedByDescending { it }
//}

//fun initializeNodeRadii(deck: Deck): (Graph) -> Graph = { graph ->
//  graph.copy(
//      nodes = graph.nodes.mapValues { (id, node) ->
//        node.copy(
//            radius = calculateInitialNodeRadius(deck, id, node)
//        )
//      }
//  )
//}

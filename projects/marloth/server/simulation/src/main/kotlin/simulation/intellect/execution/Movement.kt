package simulation.intellect.execution

import mythic.ent.Id
import simulation.intellect.Path
import simulation.intellect.assessment.Knowledge
import mythic.spatial.Vector3
import org.recast4j.detour.DefaultQueryFilter
import simulation.entities.getActiveAction
import simulation.main.World
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.Commands
import simulation.intellect.Pursuit
import simulation.intellect.design.getActionRange
import simulation.intellect.navigation.asRecastVector3
import simulation.intellect.navigation.fromRecastVector3
import simulation.main.Deck
import simulation.misc.Graph

fun doorwayPosition(graph: Graph, firstNode: Id, secondNode: Id): Vector3 {
  val a = graph.nodes[firstNode]!!
  val b = graph.nodes[secondNode]!!
  val vector = (b.position - a.position).normalize()
  return a.position + vector * a.radius
}

fun getPathTargetPosition(world: World, character: Id, pursuit: Pursuit): Vector3? {
  val body = world.deck.bodies[character]!!
  val query = world.navMeshQuery
  if (query == null)
    throw Error("Missing navMeshQuery")

  val start = asRecastVector3(body.position)
  val end = asRecastVector3(pursuit.targetPosition!!)
  val polygonRange = floatArrayOf(10f, 10f, 10f)
  val queryFilter = DefaultQueryFilter()
  val startPolygon = query.findNearestPoly(start, polygonRange, queryFilter)
  if (startPolygon.failed() || startPolygon.result.nearestRef == 0L)
    return null

  val endPolygon = query.findNearestPoly(end, polygonRange, queryFilter)
  if (endPolygon.failed() || endPolygon.result.nearestRef == 0L)
    return null

  val path = query.findPath(
      startPolygon.result.nearestRef,
      endPolygon.result.nearestRef,
      start,
      end,
      queryFilter
  )

  if (path.failed())
    return null

  val pathResult = query.findStraightPath(start, end, path.result, 2, 0)
  if (pathResult.failed())
    return null

//  assert(pathResult != null)
//  assert(pathResult.result != null)
//  assert(pathResult.result.size > 0)
//  assert(pathResult.result[0] != null)
  val firstPoint = fromRecastVector3(pathResult.result[0].pos)
  return if (firstPoint.distance(body.position) < 0.1f) {
    assert(pathResult.result.size > 1)
    fromRecastVector3(pathResult.result[1].pos)
  } else
    firstPoint
}

fun getPathTargetPositionOld(world: World, character: Id, path: Path): Vector3 {
  val graph = world.realm.graph
  val body = world.deck.bodies[character]!!
  val doorway = doorwayPosition(graph, body.nearestNode, path.first())
  val horizontalDistance = body.position.copy(z = 0f).distance(doorway.copy(z = 0f))
//  println("dist " + horizontalDistance + " " + body.nearestNode)
  return if (horizontalDistance > 0.5f)
    doorway
  else
    graph.nodes[path.first()]!!.position
}


fun moveStraightTowardPosition(world: World, character: Id, target: Vector3): Commands {
  val body = world.deck.bodies[character]!!
  val position = body.position
  val offset = (target - position).normalize()
  return spiritNeedsFacing(world, character, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, character))
  }
}

fun moveSpirit(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val target = getPathTargetPosition(world, character, pursuit)
  return if (target != null)
    moveStraightTowardPosition(world, character, target)
  else
    listOf()
}

fun isTargetInRange(world: World, character: Id, target: Id): Boolean {
  val deck = world.deck
  val characterPosition = deck.bodies[character]?.position!!
  val targetPosition = deck.bodies[target]?.position!!
  val action = getActiveAction(deck, character)
  val range = getActionRange(deck, world.definitions, action!!) - spiritAttackRangeBuffer
  val distance = characterPosition.distance(targetPosition)
  return distance <= range
}

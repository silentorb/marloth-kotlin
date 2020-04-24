package simulation.intellect.execution

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import simulation.intellect.Path
import simulation.intellect.assessment.Knowledge
import silentorb.mythic.spatial.Vector3
import org.recast4j.detour.DefaultQueryFilter
import simulation.happenings.getActiveAction
import simulation.main.World
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Commands
import simulation.intellect.Pursuit
import simulation.intellect.design.getActionRange
import silentorb.mythic.intellect.navigation.asRecastVector3
import silentorb.mythic.intellect.navigation.fromRecastVector3

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

fun moveStraightTowardPosition(world: World, character: Id, target: Vector3): Commands {
  val body = world.deck.bodies[character]!!
  val shape = world.deck.collisionShapes[character]!!
  val middle = shape.shape.height / 2f
  val position = body.position
  val commands = listOf(CharacterCommand(CharacterCommands.moveUp, character))
  if (target.x == position.x && target.y == position.y)
    return commands

  val offset = (target - position).copy(z = 0f).normalize()
  return spiritNeedsFacing(world, character, offset, 0.1f) {
    commands
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
  if (action == null)
    return false

  val range = getActionRange(deck, world.definitions, action) - spiritAttackRangeBuffer
  val distance = characterPosition.distance(targetPosition)
  return distance <= range
}

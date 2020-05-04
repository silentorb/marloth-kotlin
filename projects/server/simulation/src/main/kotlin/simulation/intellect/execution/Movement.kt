package simulation.intellect.execution

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.CharacterCommands
import org.recast4j.detour.DefaultQueryFilter
import silentorb.mythic.accessorize.getAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import silentorb.mythic.intellect.navigation.asRecastVector3
import silentorb.mythic.intellect.navigation.fromRecastVector3
import silentorb.mythic.spatial.Vector3
import simulation.happenings.TryActionEvent
import simulation.happenings.canUse
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.design.actionsForTarget
import simulation.intellect.design.getActionRange
import simulation.main.World

fun getPathTargetPosition(world: World, character: Id, pursuit: Pursuit): Vector3? {
  val body = world.deck.bodies[character]!!
  val shape = world.deck.collisionObjects[character]!!.shape
  val query = world.navigation?.query
  if (query == null)
    throw Error("Missing navMeshQuery")

  val start = asRecastVector3(body.position + Vector3(0f, 0f, -shape.height / 2f))
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
  val nextPoint = fromRecastVector3(pathResult.result.last().pos)
  return if (nextPoint.distance(body.position) < 0.1f) {
    assert(pathResult.result.size > 1)
    fromRecastVector3(pathResult.result[1].pos)
  } else
    nextPoint
}

fun tryUseDash(world: World, actor: Id): Events {
  val deck = world.deck
  val dash = getAccessory(AccessoryId.dash, deck.accessories, actor)
  return if (dash != null && canUse(world, dash))
    listOf(
        TryActionEvent(
            actor = actor,
            action = dash
        )
    )
  else
    listOf()
}

fun moveStraightTowardPosition(world: World, actor: Id, target: Vector3): Events {
  val body = world.deck.bodies[actor]!!
  val shape = world.deck.collisionObjects[actor]!!
  val middle = shape.shape.height / 2f
  val position = body.position
  val commands = listOf(CharacterCommand(CharacterCommands.moveUp, actor))
  if (target.x == position.x && target.y == position.y)
    return commands

  val vector = (target - position)
  val offset = vector.copy(z = 0f).normalize()
  val withDash = if (vector.length() > 0.5f)
    tryUseDash(world, actor)
  else
    listOf()
  return spiritNeedsFacing(world, actor, offset, 0.1f) {
    commands
  } + withDash
}

fun moveSpirit(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Events {
  val target = getPathTargetPosition(world, character, pursuit)
  return if (target != null)
    moveStraightTowardPosition(world, character, target)
  else
    listOf()
}

fun isTargetInRange(world: World, actor: Id, target: Id): Boolean {
  val deck = world.deck
  val characterPosition = deck.bodies[actor]?.position!!
  val targetPosition = deck.bodies[target]?.position!!
  val actions = actionsForTarget(world, actor, target)
  return if (actions.none())
    false
  else {
    val range = actions.map(getActionRange(deck, world.definitions)).min()!! - spiritAttackRangeBuffer
    val distance = characterPosition.distance(targetPosition)
    distance <= range
  }
}

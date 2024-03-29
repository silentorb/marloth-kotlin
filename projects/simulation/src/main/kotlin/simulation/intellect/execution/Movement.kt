package simulation.intellect.execution

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.CharacterRigCommands
import org.recast4j.detour.DefaultQueryFilter
import org.recast4j.detour.StraightPathItem
import simulation.accessorize.getAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.intellect.navigation.toRecastVector3
import silentorb.mythic.intellect.navigation.fromRecastVector3
import silentorb.mythic.spatial.Vector3
import simulation.happenings.TryActionEvent
import simulation.happenings.canUse
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.design.actionsForTarget
import simulation.intellect.design.getActionRange
import simulation.intellect.navigation.nearestPolygon
import simulation.main.World

fun getPath(world: World, actor: Id, targetPosition: Vector3): List<StraightPathItem> {
  val body = world.deck.bodies[actor]!!
  val shape = world.deck.collisionObjects[actor]!!.shape
  val query = world.navigation?.query
  if (query == null)
    throw Error("Missing navMeshQuery")

  val startOriginal = body.position + Vector3(0f, 0f, -shape.height / 2f)
  val start = toRecastVector3(startOriginal)
  val end = toRecastVector3(targetPosition)
  val queryFilter = DefaultQueryFilter()
  val startPolygon = nearestPolygon(world.navigation, startOriginal)
  if (startPolygon == null)
    return listOf()

  val endPolygon = nearestPolygon(world.navigation, targetPosition)
  if (endPolygon == null)
    return listOf()

  val path = query.findPath(
      startPolygon.result.nearestRef,
      endPolygon.result.nearestRef,
      start,
      end,
      queryFilter
  )

  if (path.failed())
    return listOf()

  val pathResult = query.findStraightPath(start, end, path.result, 2, 0)
  if (pathResult.failed())
    return listOf()

  return pathResult.result
//  assert(pathResult != null)
//  assert(pathResult.result != null)
//  assert(pathResult.result.size > 0)
//  assert(pathResult.result[0] != null)
}

fun getPathTargetPosition(world: World, actor: Id, targetPosition: Vector3): Vector3? {
  val body = world.deck.bodies[actor]!!
  val path = getPath(world, actor, targetPosition)
  return if (path.none())
    null
  else {
    val nextPoint = fromRecastVector3(path.last().pos)
    if (nextPoint.distance(body.position) < 0.1f) {
      assert(path.size > 1)
      fromRecastVector3(path[1].pos)
    } else
      nextPoint
  }
}

fun tryUseDash(world: World, actor: Id): Events {
  val deck = world.deck
  val dash = getAccessory(AccessoryIdOld.dash, deck.accessories, actor)?.key
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
  val commands = listOf(Command(CharacterRigCommands.moveForward, target = actor, value = 1f))
  if (target.x == position.x && target.y == position.y)
    return commands

  val vector = (target - position)
  val navigationDirection = world.deck.navigationDirections[actor] ?: vector
  val direction = if (navigationDirection == Vector3.zero)
    vector
  else
    navigationDirection

  val offset = direction.copy(z = 0f).normalize()
  val withDash = if (vector.length() > 0.5f)
    tryUseDash(world, actor)
  else
    listOf()
  return spiritNeedsFacing(world, actor, offset, 0.1f) {
    commands
  } + withDash
}

fun moveSpirit(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Events {
  val target = getPathTargetPosition(world, character, pursuit.targetPosition!!)
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

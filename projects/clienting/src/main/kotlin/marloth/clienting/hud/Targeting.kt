package marloth.clienting.hud

import marloth.clienting.Client
import marloth.clienting.rendering.createCamera
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.lookinglass.createCameraMatrix
import silentorb.mythic.lookinglass.getPlayerViewports
import silentorb.mythic.physics.firstRayHit
import silentorb.mythic.spatial.*
import simulation.main.Deck
import simulation.main.World
import simulation.physics.CollisionGroups

const val toggleTargetingCommand = "toggleTargeting"

typealias TargetTable = Table<Id>

typealias IsAvailableTarget = (Id) -> Boolean

const val maxTargetRange = 25f

fun isOnScreen(transform: Matrix, target: Vector3): Boolean {
  val coordinate = transform * Vector4(target.x, target.y, target.z, 1f)
  val normalized = coordinate.xyz / coordinate.w
//  println("$coordinate $k")
  val result = normalized.x > -1f && normalized.x < 1f && normalized.y > -1f && normalized.y < 1f && coordinate.w > 0
  if (!result) {
    println("$coordinate $normalized")
  }
  return result
//  return false
}

fun isAvailableTarget(world: World, transform: Matrix, actorLocation: Vector3): IsAvailableTarget = { target ->
  val deck = world.deck
  val targetBody = deck.bodies[target]!!
  actorLocation.distance(targetBody.position) <= maxTargetRange &&
      isOnScreen(transform, targetBody.position)
      firstRayHit(world.bulletState.dynamicsWorld, actorLocation, targetBody.position, CollisionGroups.tangibleMask)?.collisionObject == target
}

fun autoSelectTarget(deck: Deck, actorLocation: Vector3, actor: Id, options: List<Id>): Id {
  val characterRig = deck.characterRigs[actor]!!
  val end = actorLocation + characterRig.facingQuaternion.transform(Vector3(maxTargetRange, 0f, 0f))
  return options.minBy { target ->
    val location = deck.bodies[target]!!.position
    getPointToLineDistance(location, actorLocation, end)
  }!!
}

fun updateTargeting(world: World, client: Client, players: List<Id>, commands: HaftCommands, targets: TargetTable): TargetTable {
  val deck = world.deck
  val toggleEvents = commands.filter { it.type == toggleTargetingCommand }.map { it.target }.toSet()

  val newTargets = toggleEvents
      .filter { !targets.containsKey(it) }

  val candidates = targets.keys.minus(toggleEvents) + newTargets

  return if (candidates.none())
    mapOf()
  else {
    val viewports = getPlayerViewports(players.size, client.getWindowInfo().dimensions)
    candidates
        .mapNotNull { actor ->
          val camera = createCamera(deck, actor)
          val dimensions = viewports[players.indexOf(actor)].zw
          val transform = createCameraMatrix(dimensions, camera)
          val actorLocation = deck.bodies[actor]!!.position
          val isAvailableTarget = isAvailableTarget(world, transform, actorLocation)
          val previousTarget = targets[actor]
          val nextTarget = if (previousTarget != null && isAvailableTarget(previousTarget))
            previousTarget
          else {
            val availableTargets = deck.characters.keys
                .minus(actor)
                .filter(isAvailableTarget)

            if (availableTargets.none())
              null
            else {
              autoSelectTarget(deck, actorLocation, actor, availableTargets)
            }
          }

          if (nextTarget == null)
            null
          else
            Pair(actor, nextTarget)
        }
        .associate { it }
  }
}

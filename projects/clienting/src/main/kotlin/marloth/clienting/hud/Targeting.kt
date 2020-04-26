package marloth.clienting.hud

import marloth.clienting.Client
import marloth.clienting.rendering.createCamera
import silentorb.mythic.characters.CharacterRigCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.CommandName
import silentorb.mythic.lookinglass.createCameraMatrix
import silentorb.mythic.lookinglass.getPlayerViewports
import silentorb.mythic.physics.firstRayHit
import silentorb.mythic.spatial.*
import simulation.main.Deck
import simulation.main.World
import simulation.physics.CollisionGroups

const val toggleTargetingCommand = "toggleTargeting"

typealias TargetTable = Table<Id>

data class ScreenTarget(
    val id: Id,
    val offset: Vector2
)

typealias MapAvailableTarget = (Id) -> ScreenTarget?

const val maxTargetRange = 25f

fun transformToScreen(transform: Matrix, target: Vector3): Vector2? {
  val coordinate = transform * Vector4(target.x, target.y, target.z, 1f)

  // The w condition filters out targets behind the camera
  return if (coordinate.w > 0f)
    coordinate.xy() / coordinate.w
  else
    null
}

fun isOnScreen(transform: Matrix, target: Vector3): Boolean {
  val coordinate = transform * Vector4(target.x, target.y, target.z, 1f)
  val normalized = coordinate.xyz / coordinate.w

  // The w condition filters out targets behind the camera
  return normalized.x > -1f && normalized.x < 1f && normalized.y > -1f && normalized.y < 1f && coordinate.w > 0f
}

fun isOnScreen(offset: Vector2): Boolean {
  return offset.x > -1f && offset.x < 1f && offset.y > -1f && offset.y < 1f
}

fun mapAvailableTarget(world: World, transform: Matrix, actorLocation: Vector3): MapAvailableTarget = { target ->
  val deck = world.deck
  val targetBody = deck.bodies[target]!!
  if (actorLocation.distance(targetBody.position) <= maxTargetRange) {
    val offset = transformToScreen(transform, targetBody.position)
    if (offset != null && isOnScreen(offset) && firstRayHit(world.bulletState.dynamicsWorld, actorLocation, targetBody.position, CollisionGroups.tangibleMask)?.collisionObject == target) {
      ScreenTarget(target, offset)
    } else
      null
  } else
    null
}

fun getAvailableTargets(mapAvailableTarget: MapAvailableTarget, actor: Id, ids: Set<Id>): List<ScreenTarget> =
    ids
        .minus(actor)
        .mapNotNull(mapAvailableTarget)

fun autoSelectTarget(deck: Deck, actorLocation: Vector3, actor: Id, options: List<ScreenTarget>): Id {
  return if (options.size == 1)
    options.first().id
  else {
//    val characterRig = deck.characterRigs[actor]!!
//    val end = actorLocation + characterRig.facingOrientation.transform(Vector3(maxTargetRange, 0f, 0f))
    options.minBy { target ->
      target.offset.length()
    }!!.id
  }
}

fun integrateCommands(commands: List<CharacterCommand>, type: CommandName): Float =
    commands.filter { it.type == type }.sumByDouble { it.value.toDouble() }.toFloat()

fun getTargetChangeDirection(commands: List<CharacterCommand>): Vector2 {
  return Vector2(
      x = integrateCommands(commands, CharacterRigCommands.lookRight) - integrateCommands(commands, CharacterRigCommands.lookLeft),
      y = integrateCommands(commands, CharacterRigCommands.lookUp) - integrateCommands(commands, CharacterRigCommands.lookDown)
  )
}

fun checkTargetChange(world: World, mapAvailableTarget: MapAvailableTarget, screenTransform: Matrix,
                      actor: Id, actorLocation: Vector3,
                      commands: List<CharacterCommand>, previousCommands: List<HaftCommand>, previousTarget: Id): Id {
  val changeDirection = getTargetChangeDirection(commands.filter { it.target == actor })
  return if (changeDirection == Vector2.zero)
    previousTarget
  else {
    val filteredPreviousCommands = previousCommands
        .filter { it.target == actor }
        .map { CharacterCommand(it.type.toString(), it.target, it.value) }

    val previousDirection = getTargetChangeDirection(filteredPreviousCommands)
    if (previousDirection != Vector2.zero)
      previousTarget
    else {
      val availableTargets = getAvailableTargets(mapAvailableTarget, actor, world.deck.characters.keys)

      if (availableTargets.size == 1)
        previousTarget
      else {
        val direction = changeDirection.normalize()
        val origin = transformToScreen(screenTransform, actorLocation)!!
        val mapped = availableTargets
            .mapNotNull { option ->
              val offset = (option.offset - origin)
              val dot = direction.dot(offset.normalize())
              if (dot < 0.6f)
                null
              else
                Pair(option, offset.length())
            }

        mapped.minBy { it.second }?.first?.id ?: previousTarget
      }
    }
  }
}

fun updateTargeting(world: World, client: Client, players: List<Id>, commands: List<CharacterCommand>, previousCommands: List<HaftCommand>, targets: TargetTable): TargetTable {
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
          val screenTransform = createCameraMatrix(dimensions, camera)
          val actorLocation = deck.bodies[actor]!!.position
          val mapAvailableTarget = mapAvailableTarget(world, screenTransform, actorLocation)
          val previousTarget = targets[actor]
          val nextTarget = if (previousTarget != null && mapAvailableTarget(previousTarget) != null)
            checkTargetChange(world, mapAvailableTarget, screenTransform, actor, actorLocation, commands, previousCommands, previousTarget)
          else {
            val availableTargets = getAvailableTargets(mapAvailableTarget, actor, deck.characters.keys)

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

package simulation.combat.spatial

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.audio.NewSound
import silentorb.mythic.characters.defaultCharacterHeight
import simulation.combat.general.AttackMethod
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.spatial.Vector3

const val executeMarker = "execute"

data class AttackEvent(
    val attacker: Id,
    val accessory: AccessoryName,
    val target: Vector3? = null
) : GameEvent

fun onAttack(world: SpatialCombatWorld): (AttackEvent) -> Events = { event ->
  val (definitions, deck, bulletState) = world
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = definitions.weapons[accessory]!!
  val (origin, _) = getAttackerOriginAndFacing(world.deck, attacker, event.target, 0.5f)
  val attackEvents = when (weapon.attackMethod) {
    AttackMethod.raycast -> raycastAttack(world, attacker, weapon, event.target)
    AttackMethod.missile -> missileAttack(world, attacker, weapon, event.target)
    AttackMethod.melee -> meleeAttack(world, attacker, weapon, event.target, event.accessory)
    else -> throw Error("Not implemented")
  }
  if (weapon.sound != null)
    attackEvents
        .plus(listOf(
            NewSound(
                type = weapon.sound,
                volume = 1f,
                position = origin
            )
        ))
  else
    attackEvents
}

fun startAttack(attacker: Id, action: Id, accessory: AccessoryName, target: Vector3?): UseAction {
  return UseAction(
      actor = attacker,
      action = action,
      deferredEvents = mapOf(
          executeMarker to AttackEvent(
              attacker = attacker,
              accessory = accessory,
              target = target
          )
      )
  )
}

fun getAttackerOriginAndFacing(deck: SpatialCombatDeck, attacker: Id, externalTarget: Vector3?, forwardOffset: Float): Pair<Vector3, Vector3> {
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val target = externalTarget ?: deck.bodies[deck.targets[attacker]]?.position
  val baseOrigin = body.position + Vector3(0f, 0f, defaultCharacterHeight * 0.5f)
  val vector = if (target == null)
    characterRig.facingVector
  else
    (target - baseOrigin).normalize()

  val origin = baseOrigin + vector * forwardOffset
  return Pair(origin, vector)
}

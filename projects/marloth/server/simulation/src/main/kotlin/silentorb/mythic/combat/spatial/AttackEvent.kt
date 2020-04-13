package silentorb.mythic.combat.spatial

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.audio.NewSound
import silentorb.mythic.characters.defaultCharacterHeight
import silentorb.mythic.combat.general.AttackMethod
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.spatial.Vector3

const val attackMarker = "attack"

data class AttackEvent(
    val attacker: Id,
    val accessory: AccessoryName
) : GameEvent

fun onAttack(world: SpatialCombatWorld): (AttackEvent) -> Events = { event ->
  val (definitions, deck, bulletState) = world
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = definitions.weapons[accessory]!!
  val (origin, _) = getAttackerOriginAndFacing(world.deck, attacker, 0.5f)
  val attackEvents = when (weapon.attackMethod) {
    AttackMethod.raycast -> raycastAttack(world, attacker, weapon)
    AttackMethod.missile -> missileAttack(world, attacker, weapon)
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

fun startAttack(attacker: Id, action: Id, accessory: AccessoryName): Events {
  return listOf(
      UseAction(
          actor = attacker,
          action = action,
          deferredEvents = mapOf(
              attackMarker to AttackEvent(
                  attacker = attacker,
                  accessory = accessory
              )
          )
      )
  )
}

fun getAttackerOriginAndFacing(deck: SpatialCombatDeck, attacker: Id, forwardOffset: Float): Pair<Vector3, Vector3> {
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val vector = characterRig.facingVector
  val origin = body.position + Vector3(0f, 0f, defaultCharacterHeight * 0.75f) + vector * forwardOffset
  return Pair(origin, vector)
}

package simulation.combat.spatial

import silentorb.mythic.audio.NewSound
import silentorb.mythic.characters.rigs.defaultCharacterHeight
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.performing.ActionDefinition
import silentorb.mythic.spatial.Vector3
import simulation.accessorize.AccessoryName
import simulation.combat.general.AttackMethod
import simulation.happenings.UseAction

const val executeMarker = "execute"

data class AttackEvent(
    val attacker: Id,
    val accessory: AccessoryName,
    val targetLocation: Vector3? = null,
    val targetEntity: Id? = null,
)

fun onAttack(world: SpatialCombatWorld): (AttackEvent) -> Events = { event ->
  val (definitions, deck, bulletState) = world
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = definitions.weapons[accessory]!!
  val (origin, _) = getAttackerOriginAndFacing(world.deck, attacker, event.targetLocation, event.targetEntity, 0.5f)
  val attackEvents = when (weapon.attackMethod) {
    AttackMethod.raycast -> raycastAttack(world, attacker, weapon, event.targetLocation, event.targetEntity)
    AttackMethod.missile, AttackMethod.lobbed -> missileAttack(world, attacker, weapon, event.targetLocation, event.targetEntity)
    AttackMethod.melee -> meleeAttack(world, attacker, weapon, event.targetLocation, event.accessory)
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

fun startAttack(actionDefinition: ActionDefinition, attacker: Id, action: Id, accessory: AccessoryName,
                targetLocation: Vector3?,
                targetEntity: Id?
): GameEvent {
  val attackEvent = AttackEvent(
      attacker = attacker,
      accessory = accessory,
      targetLocation = targetLocation,
      targetEntity = targetEntity,
  )

  return if (actionDefinition.animation == null)
    attackEvent
  else
    UseAction(
        actor = attacker,
        action = action,
        deferredEvents = mapOf(
            executeMarker to attackEvent
        )
    )
}

fun getAttackerOriginAndFacing(deck: SpatialCombatDeck, attacker: Id, targetLocation: Vector3?, targetEntity: Id?,
                               forwardOffset: Float): Pair<Vector3, Vector3> {
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val target = deck.bodies[targetEntity]?.position
      ?: targetLocation
      ?: deck.bodies[deck.targets[attacker]]?.position
  val baseOrigin = body.position + Vector3(0f, 0f, defaultCharacterHeight * 0.5f)
  val vector = if (target == null)
    characterRig.facingVector
  else
    (target - baseOrigin).normalize()

  val origin = baseOrigin + vector * forwardOffset
  return Pair(origin, vector)
}

package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe2
import silentorb.mythic.spatial.*
import org.joml.times
import marloth.scenery.enums.ResourceId
import marloth.scenery.enums.Sounds
import silentorb.mythic.physics.Body
import simulation.combat.DamageMultipliers
import simulation.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.input.*
import simulation.main.Deck
import simulation.main.World
import simulation.misc.*
import silentorb.mythic.physics.LinearImpulse
import simulation.physics.characterMovementFp
import simulation.updating.simulationDelta

const val groundedLinearDamping = 0.9f
const val airLinearDamping = 0f
const val airControlReduction = 0.4f

data class CharacterDefinition(
    val health: Int,
    val maxSpeed: Float,
    val accessories: List<AccessoryName>,
    val depictionType: DepictionType,
    val deathSound: Sounds,
    val ambientSounds: List<Sounds> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf()
)

data class Character(
    val definition: CharacterDefinition,
    val turnSpeed: Vector2,
    val faction: Id,
    val sanity: ResourceContainer,
    val isAlive: Boolean = true,
    val lookVelocity: Vector2 = Vector2(),
    val activeAccessory: Id? = null,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val money: Int = 0
)

// Currently this is such a simple function because it will likely get more complicated and I want to ensure
// everything is already routing through a single point before things get more complicated.
fun isAlive(health: Int): Boolean =
    health > 0

val equipCommandSlots: Map<CommandType, Int> = listOf(
    CommandType.equipSlot0,
    CommandType.equipSlot1,
    CommandType.equipSlot2,
    CommandType.equipSlot3
).mapIndexed { index, commandType -> Pair(commandType, index) }
    .associate { it }

fun updateEquippedItem(deck: Deck, id: Id, activeAccessory: Id?, commands: Commands): Id? {
  val slot = commands
      .mapNotNull { equipCommandSlots[it.type] }
      .firstOrNull()

  return if (slot != null) {
    val itemId = getItemInSlot(deck, id, slot)
    if (itemId == activeAccessory)
      null
    else
      itemId
  } else if (activeAccessory == null) {
    val result = getTargetAttachments(deck, id).entries.firstOrNull { deck.actions.keys.contains(it.key) }?.key
    result
  } else
    activeAccessory
}

fun getPurchaseCost(deck: Deck, events: Events, character: Id): Int {
  val purchases = events.filterIsInstance<PurchaseEvent>()
      .filter { it.customer == character }

  return purchases.map { purchase ->
    val ware = deck.wares[purchase.ware]!!
    ware.price
  }
      .sum()
}

fun getMoneyFromTakenItems(deck: Deck, events: Events, character: Id): Int {
  val takes = events.filterIsInstance<TakeItemEvent>().filter { it.actor == character }.map { it.item }
  val moneyTakes = deck.resources.filterKeys { takes.contains(it) }
  return moneyTakes
      .mapNotNull { it.value.values[ResourceId.money] }
      .sum()
}

fun updateMoney(deck: Deck, events: Events, character: Id, money: Int): Int {
  val moneyFromItems = getMoneyFromTakenItems(deck, events, character)
  val cost = getPurchaseCost(deck, events, character)
  return money - cost + moneyFromItems
}

fun updateInteractingWith(deck: Deck, character: Id, commands: Commands, interactingWith: Id?): Id? =
    if (commands.any { it.type == CommandType.interactPrimary })
      deck.characters[character]!!.canInteractWith
    else if (commands.any { it.type == CommandType.stopInteracting } ||
        (interactingWith != null && !deck.interactables.containsKey(interactingWith)))
      null
    else
      interactingWith

fun updateCharacter(deck: Deck, id: Id, character: Character, commands: Commands, events: Events,
                    delta: Float): Character {
  val lookForce = characterLookForce(character, commands)

  val destructible = deck.destructibles[id]!!
  val isAlive = isAlive(destructible.health.value)
  val justDied = !isAlive && character.isAlive

  return pipe2(character, listOf(
      { c ->
        c.copy(
            isAlive = isAlive,
            activeAccessory = updateEquippedItem(deck, id, character.activeAccessory, commands),
            interactingWith = updateInteractingWith(deck, id, commands, c.interactingWith),
            money = updateMoney(deck, events, id, character.money)
        )
      },
      { c ->
        if (justDied) {
          if (destructible.lastDamageSource != 0L) {
            val source = destructible.lastDamageSource
            val killerBody = deck.bodies[source]
            if (killerBody != null) {
              val facingVector = (killerBody.position - deck.bodies[id]!!.position).normalize()
              val lookAtAngle = getLookAtAngle(facingVector)
              c.copy(
                  lookVelocity = Vector2(),
                  facingRotation = Vector3(0f, 0f, lookAtAngle)
              )
            } else
              c
          } else
            c
        } else {
          val lookVelocity = transitionVector(maxNegativeLookVelocityChange(), maxPostiveLookVelocityChange(),
              character.lookVelocity, lookForce)
          val facingRotation = character.facingRotation + fpCameraRotation(lookVelocity, delta)

          c.copy(
              lookVelocity = lookVelocity,
              facingRotation = Vector3(0f, facingRotation.y, facingRotation.z)
          )
        }
      }
  ))
}

fun updateCharacter(deck: Deck, commands: Commands, events: Events): (Id, Character) -> Character = { id, character ->
  val delta = simulationDelta
  if (commands.any()) {
    val k = 0
  }

  val characterCommands = pipe2(commands, listOf(
      { c -> if (deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))

  updateCharacter(deck, id, character, characterCommands, events, delta)
}

fun getMovementImpulseVector(baseSpeed: Float, velocity: Vector3, commandVector: Vector3): Vector3 {
  val rawImpulseVector = commandVector * 1.5f - velocity
  val finalImpulseVector = if (rawImpulseVector.length() > baseSpeed)
    rawImpulseVector.normalize() * baseSpeed
  else
    rawImpulseVector

  return finalImpulseVector
}

fun allCharacterMovements(world: World, commands: Commands): List<LinearImpulse> =
    world.deck.characters
        .filter { world.deck.characters[it.key]!!.isAlive }
        .mapNotNull { characterMovementFp(filterCommands(it.key, commands), it.value, it.key, world.deck.bodies[it.key]!!) }

fun allCharacterOrientations(world: World): List<AbsoluteOrientationForce> =
    world.deck.characters.map {
      AbsoluteOrientationForce(it.key, Quaternion()
          .rotateZ(it.value.facingRotation.z))
    }


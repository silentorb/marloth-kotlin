package simulation.entities

import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.pipe
import mythic.spatial.*
import org.joml.times
import scenery.AnimationId
import scenery.Capsule
import scenery.enums.ResourceId
import scenery.enums.Sounds
import simulation.combat.DamageMultipliers
import simulation.happenings.OrganizedEvents
import simulation.input.*
import simulation.intellect.Spirit
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.World
import simulation.main.simulationDelta
import simulation.misc.ResourceContainer
import simulation.misc.maxNegativeLookVelocityChange
import simulation.misc.maxPostiveLookVelocityChange
import simulation.physics.*
import simulation.physics.old.*

//const val characterGroundBuffer = 0.01f
const val characterGroundBuffer = 0.02f
const val groundedLinearDamping = 0.9f
const val airLinearDamping = 0f
const val airControlReduction = 0.4f

data class CharacterDefinition(
    val health: Int,
    val maxSpeed: Float,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType,
    val deathSound: Sounds,
    val ambientSounds: List<Sounds> = listOf(),
    val damageMultipliers: DamageMultipliers = mapOf()
)

data class Character(
    val definition: CharacterDefinition,
    val turnSpeed: Vector2,
    val abilities: List<Ability> = listOf(),
    val faction: Id,
    val sanity: ResourceContainer,
    val isAlive: Boolean = true,
    val facingRotation: Vector3 = Vector3(),
    val lookVelocity: Vector2 = Vector2(),
    val activeItem: Id? = null,
    val canInteractWith: Id? = null,
    val interactingWith: Id? = null,
    val money: Int = 0,
    val groundDistance: Float = 0f
) {
  val isGrounded: Boolean get() = groundDistance <= characterGroundBuffer
  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

data class ArmatureAnimation(
    val animationIndex: Int,
    var timeOffset: Float
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

fun updateEquippedItem(deck: Deck, id: Id, character: Character, commands: Commands): Id? {
  val slot = commands
      .mapNotNull { equipCommandSlots[it.type] }
      .firstOrNull()

  return if (slot != null) {
    val itemId = getItemInSlot(deck, id, slot)
    if (itemId == character.activeItem)
      null
    else
      itemId
  } else
    character.activeItem
}

fun getPurchaseCost(deck: Deck, events: OrganizedEvents, character: Id): Int {
  val purchases = events.purchases.filter { it.customer == character }
  return purchases.map { purchase ->
    val ware = deck.wares[purchase.ware]!!
    ware.price
  }
      .sum()
}

fun getMoneyFromTakenItems(deck: Deck, events: OrganizedEvents, character: Id): Int {
  val takes = events.takeItems.filter { it.actor == character }.map { it.item }
  val moneyTakes = deck.resources.filterKeys { takes.contains(it) }
  return moneyTakes
      .mapNotNull { it.value.values[ResourceId.money] }
      .sum()
}

fun updateMoney(deck: Deck, events: OrganizedEvents, character: Id, money: Int): Int {
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

fun updateCharacter(deck: Deck, id: Id, character: Character, commands: Commands, activatedAbilities: List<Ability>,
                    events: OrganizedEvents, delta: Float): Character {
  val lookForce = characterLookForce(character, commands)

  val abilities = updateAbilities(character, activatedAbilities)

  val destructible = deck.destructibles[id]!!
  val isAlive = isAlive(destructible.health.value)
  val justDied = !isAlive && character.isAlive

  return pipe(character, listOf(
      { c ->
        c.copy(
            isAlive = isAlive,
            abilities = abilities,
            activeItem = updateEquippedItem(deck, id, character, commands),
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

fun updateCharacter(deck: Deck, commands: Commands, activatedAbilities: List<ActivatedAbility>,
                    events: OrganizedEvents): (Id, Character) -> Character = { id, character ->
  val delta = simulationDelta
  val abilities = activatedAbilities.filter { it.character == id }
      .map { it.ability }

  val characterCommands = pipe(commands, listOf(
      { c -> if (deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))

  updateCharacter(deck, id, character, characterCommands, abilities, events, delta)
}

fun getMovementImpulseVector(baseSpeed: Float, velocity: Vector3, commandVector: Vector3): Vector3 {
  val rawImpulseVector = commandVector * 1.5f - velocity
  val finalImpulseVector = if (rawImpulseVector.length() > baseSpeed)
    rawImpulseVector.normalize() * baseSpeed
  else
    rawImpulseVector

  return finalImpulseVector
}

fun characterOrientationZ(character: Character) =
    Quaternion().rotateZ(character.facingRotation.z - Pi / 2)

fun characterMovementFp(commands: Commands, character: Character, id: Id, body: Body): LinearImpulse? {
  val offsetVector = joinInputVector(commands, playerMoveMap)
  return if (offsetVector != null) {
    val airControlMod = if (character.isGrounded) 1f else airControlReduction
    val direction = characterOrientationZ(character) * offsetVector * airControlMod
    val baseSpeed = character.definition.maxSpeed
    val maxImpulseLength = baseSpeed
    val commandVector = direction * maxImpulseLength
    val horizontalVelocity = body.velocity.copy(z = 0f)
    val impulseVector = getMovementImpulseVector(baseSpeed, horizontalVelocity, commandVector)
    val finalImpulse = impulseVector * 5f
    LinearImpulse(body = id, offset = finalImpulse)
  } else {
    null
  }
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

fun newCharacter(nextId: IdSource, definition: CharacterDefinition, faction: Id, position: Vector3,
                 player: Player? = null, spirit: Spirit? = null): Hand {
  val abilities = definition.abilities.map {
    Ability(
        id = nextId(),
        definition = it
    )
  }
  return Hand(
      ambientAudioEmitter = if (definition.ambientSounds.any())
        AmbientAudioEmitter(
            delay = position.length() % 2.0
        )
      else
        null,
      body = Body(
          position = position,
          orientation = Quaternion(),
          velocity = Vector3()
      ),
      character = Character(
          definition = definition,
          turnSpeed = Vector2(3f, 1f),
          facingRotation = Vector3(0f, 0f, Pi / 2f),
          faction = faction,
          sanity = ResourceContainer(100),
          abilities = abilities,
          money = 30
      ),
      destructible = Destructible(
          base = DestructibleBaseStats(
              health = definition.health,
              damageMultipliers = definition.damageMultipliers
          ),
          health = ResourceContainer(definition.health),
          damageMultipliers = definition.damageMultipliers
      ),
      collisionShape = CollisionObject(
          shape = Capsule(0.4f, 2.3f)
      ),
      depiction = Depiction(
          type = definition.depictionType,
          animations = listOf(
              DepictionAnimation(
                  animationId = AnimationId.stand,
                  animationOffset = 0f
              )
          )
      ),
      dynamicBody = DynamicBody(
          gravity = true,
          mass = 45f,
          resistance = 4f
      ),
      player = player,
      spirit = spirit
  )
}

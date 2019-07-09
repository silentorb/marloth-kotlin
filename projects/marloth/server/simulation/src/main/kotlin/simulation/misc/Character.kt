package simulation.misc

import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.pipe
import mythic.spatial.*
import org.joml.times
import scenery.AnimationId
import scenery.Capsule
import scenery.ShapeOffset
import scenery.Sounds
import simulation.changing.characterLookForce
import simulation.changing.fpCameraRotation
import simulation.combat.Damage
import simulation.combat.DamageMap
import simulation.evention.DamageEvent
import simulation.input.filterCommands
import simulation.intellect.Spirit
import simulation.main.*
import simulation.physics.*

data class CharacterDefinition(
    val health: Int,
    val maxSpeed: Float,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType,
    val deathSound: Sounds,
    val ambientSounds: List<Sounds> = listOf(),
    val damageMap: DamageMap = mapOf()
)

data class Character(
    val definition: CharacterDefinition,
    val turnSpeed: Vector2,
    val abilities: List<Ability> = listOf(),
    val faction: Id,
    val health: Resource,
    val sanity: Resource,
    val isAlive: Boolean = true,
    val facingRotation: Vector3 = Vector3(),
    val lookVelocity: Vector2 = Vector2(),
    val equippedItem: Id? = null
) {
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
    if (itemId == character.equippedItem)
      null
    else
      itemId
  } else
    character.equippedItem
}

fun aggregateDamage(character: Character, damages: List<Damage>) =
    damages.map { it.amount }.sum()

fun aggregateHealthModifiers(character: Character, damages: List<Damage>): Int {
  val damage = aggregateDamage(character, damages)
  return -damage
}

fun updateCharacter(world: World, id: Id, character: Character, commands: Commands, damages: List<Damage>,
                    activatedAbilities: List<Ability>, delta: Float): Character {
  val lookForce = characterLookForce(character, commands)

  val healthMod = aggregateHealthModifiers(character, damages)
  val health = modifyResource(character.health, healthMod)
  val abilities = updateAbilities(character, activatedAbilities)

  val isAlive = isAlive(health)
  val justDied = !isAlive && character.isAlive

  return pipe(character, listOf(
      { c ->
        c.copy(
            isAlive = isAlive,
            health = character.health.copy(value = health),
            abilities = abilities,
            equippedItem = updateEquippedItem(world.deck, id, character, commands)
        )
      },
      { c ->
        if (justDied) {
          if (damages.any()) {
            val hit = damages.first()
            val killerBody = world.bodyTable[hit.source]
            if (killerBody != null) {
              val facingVector = (killerBody. position -world.bodyTable[id]!!.position).normalize()
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
          val lookVelocity = transitionVector(maxLookVelocityChange(),
              Vector3(character.lookVelocity, 0f), Vector3(lookForce, 0f)).xy()
          val facingRotation = character.facingRotation + fpCameraRotation(lookVelocity, delta)

          c.copy(
              lookVelocity = lookVelocity,
              facingRotation = Vector3(0f, facingRotation.y, facingRotation.z)
          )
        }
      }
  ))
}

fun updateCharacter(world: World, commands: Commands, activatedAbilities: List<ActivatedAbility>,
                    damageEvents: List<DamageEvent>): (Id, Character) -> Character = { id, character ->
  val delta = simulationDelta
  val abilities = activatedAbilities.filter { it.character == id }
      .map { it.ability }

  val characterCommands = pipe(commands, listOf(
      { c -> if (world.deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))

  val damages = damageEvents
      .filter { it.target == id }
      .map { it.damage }

  updateCharacter(world, id, character, characterCommands, damages, abilities, delta)
}

fun characterMovementFp(commands: Commands, character: Character, id: Id, body: Body): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = Quaternion().rotateZ(character.facingRotation.z - Pi / 2) * offset * character.definition.maxSpeed
    return MovementForce(body = id, offset = offset)
  } else {
    return null
  }
}

fun allCharacterMovements(world: World, commands: Commands): List<MovementForce> =
    world.deck.characters
        .filter { world.deck.characters[it.key]!!.isAlive }
        .mapNotNull { characterMovementFp(filterCommands(it.key, commands), it.value, it.key, world.bodyTable[it.key]!!) }

fun allCharacterOrientations(world: World): List<AbsoluteOrientationForce> =
    world.deck.characters.map {
      AbsoluteOrientationForce(it.key, Quaternion()
          .rotateZ(it.value.facingRotation.z))
    }

fun newCharacter(id: Id, nextId: IdSource, definition: CharacterDefinition, faction: Id, position: Vector3, node: Id,
                 player: Player? = null, spirit: Spirit? = null): IdHand {
  val abilities = definition.abilities.map {
    Ability(
        id = nextId(),
        definition = it
    )
  }
  return IdHand(id, Hand(
      ambientAudioEmitter = if (definition.ambientSounds.any())
        AmbientAudioEmitter(
            delay = position.length() % 2.0
        )
      else
        null,
      body = Body(
          position = position,
          orientation = Quaternion(),
          velocity = Vector3(),
          node = node
      ),
      character = Character(
          definition = definition,
          turnSpeed = Vector2(2f, 1f),
          facingRotation = Vector3(0f, 0f, Pi / 2f),
          faction = faction,
          health = Resource(definition.health),
          sanity = Resource(100),
          abilities = abilities
      ),
      collisionShape = CollisionObject(
          shape = ShapeOffset(Matrix().translate(0f, 0f, 0.75f), Capsule(0.4f, 1.5f))
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
  ))
}

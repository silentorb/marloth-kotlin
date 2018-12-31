package simulation

import intellect.Spirit
import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.pipe
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.times
import physics.*
import scenery.AnimationId
import scenery.Sounds
import simulation.changing.*
import simulation.input.filterCommands

data class CharacterDefinition(
    val health: Int,
    val maxSpeed: Float,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType,
    val deathSound: Sounds
)

data class Character(
    override val id: Id,
    val definition: CharacterDefinition,
    val turnSpeed: Vector2,
    val abilities: List<Ability> = listOf(),
    val faction: Id,
    val health: Resource,
    val isAlive: Boolean = true,
    val facingRotation: Vector3 = Vector3(),
    val lookVelocity: Vector2 = Vector2()
) : Entity {
  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

data class ArmatureAnimation(
    override val id: Id,
    val animationIndex: Int,
    var timeOffset: Float
) : Entity

// Currently this is such a simple function because it will likely get more complicated and I want to ensure
// everything is already routing through a single point before things get more complicated.
fun isAlive(health: Int): Boolean =
    health > 0

fun updateCharacter(world: World, character: Character, commands: Commands, collisions: List<Collision>,
                    activatedAbilities: List<Ability>, delta: Float): Character {
  val lookForce = characterLookForce(character, commands)

  val hits = collisions.filter { it.second == character.id }
  val health = modifyResource(character.health, hits.map { -50 }.sum())
  val abilities = updateAbilities(character, activatedAbilities)

  val isAlive = isAlive(health)
  val justDied = !isAlive && character.isAlive

  return pipe(character, listOf(
      { c ->
        c.copy(
            isAlive = isAlive,
            health = character.health.copy(value = health),
            abilities = abilities
        )
      },
      { c ->
        if (justDied) {
          val hit = hits.first()
          val attacker = world.deck.missiles[hit.first]!!.owner
          val facingVector = (world.bodyTable[attacker]!!.position - world.bodyTable[character.id]!!.position).normalize()
          val lookAtAngle = getLookAtAngle(facingVector)
          c.copy(
              lookVelocity = Vector2(),
              facingRotation = Vector3(0f, 0f, lookAtAngle)
          )
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

fun updateCharacter(world: World, collisions: Collisions, commands: Commands, activatedAbilities: List<ActivatedAbility>): (Character) -> Character = { character ->
  val delta = simulationDelta
  val id = character.id
  val abilities = activatedAbilities.filter { it.character.id == character.id }
      .map { it.ability }

  val characterCommands = pipe(commands, listOf(
      { c -> if (world.deck.characters[id]!!.isAlive) c else listOf() },
      { c -> c.filter { it.target == id } }
  ))
  updateCharacter(world, character, characterCommands, collisions, abilities, delta)
}

//fun updateCharacters(world: World, collisions: Collisions, commands: Commands, activatedAbilities: List<ActivatedAbility>): List<Character> {
//  val delta = simulationDelta
//  return world.characterTable.map { e ->
//    val character = e.value
//    val id = character.id
//    val abilities = activatedAbilities.filter { it.character.id == character.id }
//        .map { it.ability }
//
//    val characterCommands = pipe(commands, listOf(
//        { c -> if (isAlive(world, id)) c else listOf() },
//        { c -> c.filter { it.target == id } }
//    ))
//    updateCharacter(world, character, characterCommands, collisions, abilities, delta)
//  }
//}

fun characterMovementFp(commands: Commands, character: Character, body: Body): MovementForce? {
  var offset = joinInputVector(commands, playerMoveMap)
  if (offset != null) {
    offset = Quaternion().rotateZ(character.facingRotation.z - Pi / 2) * offset * character.definition.maxSpeed
    return MovementForce(body = body.id, offset = offset)
  } else {
    return null
  }
}

fun allCharacterMovements(world: World, commands: Commands): List<MovementForce> =
    world.characters
        .filter { world.deck.characters[it.id]!!.isAlive }
        .mapNotNull { characterMovementFp(filterCommands(it.id, commands), it, world.bodyTable[it.id]!!) }

fun allCharacterOrientations(world: World): List<AbsoluteOrientationForce> =
    world.characters.map {
      AbsoluteOrientationForce(it.id, Quaternion()
          .rotateZ(it.facingRotation.z))
    }

fun newCharacter(nextId: IdSource, definition: CharacterDefinition, faction: Id, position: Vector3, node: Id,
                 player: Player? = null, spirit: Spirit? = null): Hand {
  val id = nextId()
  val abilities = definition.abilities.map {
    Ability(
        id = nextId(),
        definition = it
    )
  }
  return Hand(
      body = Body(
          id = id,
          shape = commonShapes[EntityType.character]!!,
          position = position,
          orientation = Quaternion(),
          velocity = Vector3(),
          node = node,
          attributes = characterBodyAttributes,
          gravity = true
      ),
      character = Character(
          id = id,
          definition = definition,
          turnSpeed = Vector2(2f, 1f),
          faction = faction,
          health = Resource(definition.health),
          abilities = abilities
      ),
      depiction = Depiction(
          id = id,
          type = definition.depictionType,
          animations = listOf(
              DepictionAnimation(
                  animationId = AnimationId.stand,
                  animationOffset = 0f
              )
          )
      ),
      player = player?.copy(id = id),
      spirit = spirit?.copy(id = id)
  )
}
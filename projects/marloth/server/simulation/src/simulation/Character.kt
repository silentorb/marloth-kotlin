package simulation

import intellect.NewSpirit
import intellect.Spirit
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.plus
import org.joml.times
import physics.Body
import physics.Collision
import physics.Collisions
import physics.commonShapes
import scenery.DepictionType
import simulation.changing.*

data class CharacterDefinition(
    val health: Int,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType
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
) : EntityLike {
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
) : EntityLike

fun isFinished(world: World, character: Character) =
    character.health.value == 0

fun updateCharacter(world: World, character: Character, commands: Commands, collisions: List<Collision>,
                    activatedAbilities: List<Ability>, delta: Float): Character {
  val id = character.id
  if (character.isAlive) {
    val body = world.bodyTable[character.id]!!
//    character.activatedAbilities.forEach { updateAbility(it, delta) }
    body.orientation = Quaternion()
        .rotateZ(character.facingRotation.z)

//    val animationInfo = world.animationTable[character.id]!!
//    val animation = animationInfo.armature.animations[animationInfo.animationIndex]
//    animationInfo.timeOffset = (animationInfo.timeOffset + delta) % animation.duration
//    applyAnimation(animation, animationInfo.armature.bones, animationInfo.timeOffset)
  } else {

  }
  val lookForce = characterLookForce(character, commands)
  val lookVelocity = updatePlayerLookVelocity(lookForce, character.lookVelocity)

  val hits = collisions.filter { it.second == character.id }
  val health = modifyResource(character.health, hits.map { -50 })

  val abilities = updateAbilities(character, activatedAbilities)
  val facingRotation = character.facingRotation + fpCameraRotation(lookVelocity, delta)
  return character.copy(
      isAlive = character.health.value > 0,
      lookVelocity = lookVelocity,
      facingRotation = Vector3(0f, facingRotation.y, simplifyRotation(facingRotation.z)),
      health = character.health.copy(value = health),
      abilities = abilities
  )
}

fun updateCharacters(world: World, collisions: Collisions, commands: Commands, activatedAbilities: List<ActivatedAbility>): List<Character> {
  val delta = simulationDelta
  return world.characterTable.map { e ->
    val character = e.value
    val id = character.id
    val abilities = activatedAbilities.filter { it.character.id == character.id }
        .map { it.ability }
    updateCharacter(world, character, commands.filter { it.target == id }, collisions, abilities, delta)
  }
}

fun newCharacter(nextId: IdSource, definition: CharacterDefinition, faction: Id, position: Vector3, node: Node,
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
          turnSpeed = Vector2(1.5f, 1f),
          faction = faction,
          health = Resource(definition.health),
          abilities = abilities
      ),
      player = player?.copy(character = id),
      spirit = spirit?.copy(id = id)
  )
}
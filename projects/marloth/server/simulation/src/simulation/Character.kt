package simulation

import mythic.breeze.applyAnimation
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.plus
import org.joml.times
import physics.Body
import scenery.DepictionType
import simulation.changing.*
import simulation.combat.Collision

data class CharacterDefinition(
    val health: Int,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType
)

data class Character(
    val id: Int,
    val turnSpeed: Vector2,
    val body: Body,
    val abilities: List<Ability> = listOf(),
    val faction: Faction,
    val health: Resource,
    val isAlive: Boolean = true,
    val facingRotation: Vector3 = Vector3(),
    val lookVelocity: Vector2 = Vector2()
) {
  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0

fun updateCharacter(world: World, character: Character, commands: Commands, collisions: List<Collision>,
                    activatedAbilities: List<Ability>, delta: Float): Character {
  val id = character.id
  if (character.isAlive) {
//    character.activatedAbilities.forEach { updateAbility(it, delta) }
    character.body.orientation = Quaternion()
        .rotateZ(character.facingRotation.z)

    val depiction = world.depictionTable[character.id]!!
    val animationInfo = depiction.animation!!
    val animation = animationInfo.armature.animations[animationInfo.animationIndex]
    animationInfo.timeOffset = (animationInfo.timeOffset + delta) % animation.duration
    applyAnimation(animation, animationInfo.armature.bones, animationInfo.timeOffset)
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

fun updateCharacters(world: World, collisions: List<Collision>, commands: Commands, activatedAbilities: List<ActivatedAbility>): List<Character> {
  val delta = simulationDelta
  return world.characterTable.map { e ->
    val character = e.value
    val id = character.id
    val abilities = activatedAbilities.filter { it.character.id == character.id }
        .map { it.ability }
    updateCharacter(world, character, commands.filter { it.target == id }, collisions, abilities, delta)
  }
}

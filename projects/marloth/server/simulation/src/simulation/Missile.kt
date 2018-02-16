package simulation

import commanding.CommandType
import haft.Commands
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus

data class Missile(
    val id: Int,
    val body: Body,
    val owner: Character,
    var remainingDistance: Float
)

data class NewMissile(
    val position: Vector3,
    val velocity: Vector3,
    val range: Float,
    val owner: Character
)

fun characterAttack(character: Character, ability: Ability, direction: Vector3): NewMissile {
  useAbility(ability)
  return NewMissile(
      position = character.body.position + direction * 0.5f,
      velocity = direction * 14.0f,
      range = ability.definition.range,
      owner = character
  )
}

fun playerAttack(world: World, character: Character, commands: Commands<CommandType>): NewMissile? {
  val offset = joinInputVector(commands, playerAttackMap)
  if (offset != null) {
    val ability = character.abilities[0]
    if (canUse(character, ability)) {
      return characterAttack(character, ability, offset)
    }
  }

  return null
}

fun updateMissile(world: World, missile: Missile, delta: Float) {
  val offset = missile.body.velocity * delta
  missile.body.position += offset
  val hit = world.bodyTable.values.filter { it !== missile.body && it !== missile.owner.body }
      .firstOrNull { overlaps(it, missile.body) }

  if (hit != null) {
    missile.remainingDistance = 0f
    val victim = world.characterTable[hit.id]!!
    victim.health.modify(-50)
  } else {
    missile.remainingDistance -= offset.length()
  }
}

fun isFinished(world: World, missile: Missile) =
    missile.remainingDistance <= 0 || world.meta.walls.any { hitsWall(it.edges[0], missile.body.position, 0.2f) }
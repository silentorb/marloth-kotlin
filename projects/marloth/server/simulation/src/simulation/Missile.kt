package simulation

import commanding.CommandType
import haft.Commands
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus

data class Missile(
    val id: Int,
    val body: Body,
    var remainingDistance: Float = 20f
)

data class NewMissile(
    val position: Vector3,
    val direction: Vector3
)

fun playerShoot(world: World, character: Character, commands: Commands<CommandType>): NewMissile? {
  val offset = joinInputVector(commands, playerAttackMap)

  if (offset != null) {
    val ability = character.abilities[0]
    if (canUse(character, ability)) {
      useAbility(ability)
      return NewMissile(character.body.position + offset * 0.5f, offset * 14.0f)
    }
  }

  return null
}

fun updateMissile(missile: Missile, delta: Float) {
  val offset = missile.body.velocity * delta
  missile.body.position += offset
  missile.remainingDistance -= offset.length()
}

fun isFinished(world: World, missile: Missile) =
    missile.remainingDistance <= 0 || world.meta.walls.any { hitsWall(it.edges[0], missile.body.position, 0.2f) }
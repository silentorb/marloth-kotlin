package simulation

import commanding.CommandType
import haft.Commands
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import simulation.changing.hitsWall
import simulation.changing.joinInputVector
import simulation.changing.playerAttackMap

data class Missile(
    val id: Int,
    val body: Body,
    val owner: Character,
    var remainingDistance: Float
)

data class NewMissile(
    val position: Vector3,
    val node: Node,
    val velocity: Vector3,
    val range: Float,
    val owner: Character
)

fun characterAttack(character: Character, ability: Ability, direction: Vector3): NewMissile {
  useAbility(ability)
  return NewMissile(
      position = character.body.position + direction * 0.5f + Vector3(0f, 0f, 0.7f),
      node = character.body.node,
      velocity = direction * 14.0f,
      range = ability.definition.range,
      owner = character
  )
}

fun updateMissile(world: World, missile: Missile, delta: Float) {
  val offset = missile.body.velocity * delta
  missile.body.position += offset
  val hit = world.bodyTable.values.filter { it !== missile.body && it !== missile.owner.body }
      .firstOrNull { overlaps(it, missile.body) }

  if (hit != null) {
    val victim = world.characterTable[hit.id]
    if (victim != null) {
      missile.remainingDistance = 0f
      victim.health.modify(-50)
    } else {
//      val otherMissile = world.missileTable[hit.id]
//      if (otherMissile != null) {
//        otherMissile.remainingDistance = 0f
//      }
    }
  } else {
    missile.remainingDistance -= offset.length()
  }
}

fun isFinished(world: World, missile: Missile) =
    missile.remainingDistance <= 0 || world.meta.walls.any { hitsWall(it.edges[0].edge, missile.body.position, 0.2f) }
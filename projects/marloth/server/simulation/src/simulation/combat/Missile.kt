package simulation.combat

import simulation.CommandType
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import physics.overlaps
import simulation.*
import simulation.changing.hitsWall

data class Missile(
    val id: Int,
    val owner: Id,
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

fun updateMissile(bodyTable: BodyTable, characterTable: CharacterTable, missile: Missile, delta: Float) {
  val body = bodyTable[missile.id]!!
  val offset = body.velocity * delta
//  missile.body.position += offset
  val owner = characterTable[missile.owner]!!
  val hit = bodyTable.values.filter { it !== body && it !== owner.body }
      .firstOrNull { overlaps(it, body) }

  if (hit != null) {
    val victim = characterTable[hit.id]
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

fun isFinished(world: AbstractWorld, bodyTable: BodyTable, missile: Missile): Boolean {
  val body = bodyTable[missile.id]!!.position
  return missile.remainingDistance <= 0 || world.walls.any { hitsWall(it.edges[0].edge, body, 0.2f) }
}


fun createMissiles(world: World, commands: Commands): List<Missile> {
return listOf()
}

fun updateMissiles(world: World, commands: Commands): List<Missile> {
  val newMissiles = commands.filter { it.type == CommandType.attack }
  return listOf()
}
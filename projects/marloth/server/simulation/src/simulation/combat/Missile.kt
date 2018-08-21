package simulation.combat

import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import physics.overlaps
import simulation.*
import simulation.changing.createEntity
import simulation.changing.hitsWall
import simulation.changing.simulationDelta

data class Missile(
    val id: Int,
    val owner: Id,
    val remainingDistance: Float
)

data class NewMissile(
    val id: Id,
    val position: Vector3,
    val node: Node,
    val velocity: Vector3,
    val range: Float,
    val owner: Id
)

fun characterAttack(world: World, character: Character, ability: Ability, direction: Vector3): NewMissile {
  useAbility(ability)
  return NewMissile(
      id = createEntity(world, EntityType.missile),
      position = character.body.position + direction * 0.5f + Vector3(0f, 0f, 0.7f),
      node = character.body.node,
      velocity = direction * 14.0f,
      range = ability.definition.range,
      owner = character.id
  )
}

data class Collision(
    val first: Id,
    val second: Id
)

fun getCollisions(bodyTable: BodyTable, characterTable: CharacterTable, missiles: Collection<Missile>): List<Collision> {
  return missiles.mapNotNull { missile ->
    val body = bodyTable[missile.id]!!
    val owner = characterTable[missile.owner]!!
    val hit = bodyTable.values.filter { it !== body && it !== owner.body }
        .firstOrNull { overlaps(it, body) }
    if (hit == null)
      null
    else
      Collision(missile.id, hit.id)
  }
}

fun updateMissile(bodyTable: BodyTable, characterTable: CharacterTable, missile: Missile, collisions: List<Collision>, delta: Float): Missile {
  val body = bodyTable[missile.id]!!
  val offset = body.velocity * delta
  val hit = collisions.firstOrNull { it.first == missile.id }

  val remainingDistance = if (hit != null) {
    val victim = characterTable[hit.second]
    if (victim != null) {
      0f
    } else {
      missile.remainingDistance - offset.length()
    }
  } else {
    missile.remainingDistance - offset.length()
  }
  return missile.copy(remainingDistance = remainingDistance)
}

fun isFinished(world: AbstractWorld, bodyTable: BodyTable, missile: Missile): Boolean {
  val body = bodyTable[missile.id]!!.position
  return missile.remainingDistance <= 0 || world.walls.any { hitsWall(it.edges[0].edge, body, 0.2f) }
}


fun getNewMissiles(world: World, commands: Commands): List<NewMissile> {
  return commands.filter { it.type == CommandType.attack }
      .filter {
        val character = world.characterTable[it.target]!!
        val ability = character.abilities.first()
        canUse(character, ability)
      }
      .map {
        val character = world.characterTable[it.target]!!
        val ability = character.abilities.first()
        characterAttack(world, character, ability, character.facingVector)
      }
}

fun updateMissiles(world: World, newMissiles: List<NewMissile>, collisions: List<Collision>, finished: List<Int>): List<Missile> {
  return world.missiles
      .filter { item -> finished.none { it == item.id } }
      .map { updateMissile(world.bodyTable, world.characterTable, it, collisions, simulationDelta) }
      .plus(newMissiles.map {
        val id = createEntity(world, EntityType.missile)
        Missile(id, it.owner, it.range)
      })
}
package simulation.combat

import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import physics.Collision
import physics.overlaps
import simulation.*
import simulation.changing.nextId
import simulation.changing.hitsWall
import simulation.changing.simulationDelta

data class Missile(
    override val id: Int,
    val owner: Id,
    val remainingDistance: Float
) : EntityLike

data class NewMissile(
    val id: Id,
    val position: Vector3,
    val node: Node,
    val velocity: Vector3,
    val range: Float,
    val owner: Id
)

fun characterAttack(world: WorldMap, character: Character, ability: Ability, direction: Vector3): NewMissile {
  return NewMissile(
      id = nextId(world),
      position = character.body.position + direction * 0.5f + Vector3(0f, 0f, 0.7f),
      node = character.body.node,
      velocity = direction * 14.0f,
      range = ability.definition.range,
      owner = character.id
  )
}

fun getBodyCollisions(bodyTable: BodyTable, characterTable: CharacterTable, missiles: Collection<Missile>): List<Collision> {
  return missiles.flatMap { missile ->
    val body = bodyTable[missile.id]!!
    val owner = characterTable[missile.owner]!!
    bodyTable.values.filter { it !== body && it !== owner.body }
        .filter { overlaps(it, body) }
        .map { hit ->
          Collision(
              first = missile.id,
              second = hit.id,
              hitPoint = Vector2(),
              gap = 0f
          )
        }
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
  val body = bodyTable[missile.id]!!
  val position = body.position
  return missile.remainingDistance <= 0 || world.walls.filter(isWall).any { hitsWall(it.edges[0].edge, position, body.radius!!) }
}

fun getNewMissiles(world: WorldMap, activatedAbilities: List<ActivatedAbility>): List<NewMissile> {
  return activatedAbilities.map {
    val (character, ability) = it
    characterAttack(world, character, ability, character.facingVector)
  }
}

fun updateMissiles(world: WorldMap, collisions: List<Collision>): List<Missile> {
  return world.missiles
      .map { updateMissile(world.bodyTable, world.characterTable, it, collisions, simulationDelta) }
}
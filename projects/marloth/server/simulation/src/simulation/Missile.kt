package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import physics.Body
import physics.Collision
import physics.commonShapes
import physics.overlaps
import simulation.changing.hitsWall

data class Missile(
    override val id: Id,
    val owner: Id,
    val remainingDistance: Float
) : EntityLike

fun characterAttack(world: World, nextId: IdSource, character: Character, ability: Ability, direction: Vector3): Hand {
  val body = world.bodyTable[character.id]!!
  val id = nextId()
  return Hand(
      body = Body(
          id = id,
          position = body.position + direction * 0.5f + Vector3(0f, 0f, 0.7f),
          node = body.node,
          velocity = direction * 14.0f,
          shape = commonShapes[EntityType.missile]!!,
          orientation = Quaternion(),
          attributes = missileBodyAttributes,
          gravity = false
      ),
      missile = Missile(
          id = id,
          remainingDistance = ability.definition.range,
          owner = character.id
      )
  )
}

fun getBodyCollisions(bodyTable: BodyTable, characterTable: CharacterTable, missiles: Collection<Missile>): List<Collision> {
  return missiles.flatMap { missile ->
    val body = bodyTable[missile.id]!!
    val owner = bodyTable[missile.owner]!!
    bodyTable.values.filter { it !== body && it !== owner }
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

fun isFinished(world: Realm, bodyTable: BodyTable, missile: Missile): Boolean {
  val body = bodyTable[missile.id]!!
  val position = body.position
  return missile.remainingDistance <= 0 || world.walls.filter(isWall).any { hitsWall(it.edges[0].edge, position, body.radius!!) }
}

fun getNewMissiles(world: World, nextId: IdSource, activatedAbilities: List<ActivatedAbility>): Deck {
  return toDeck(activatedAbilities.map {
    val (character, ability) = it
    characterAttack(world, nextId, character, ability, character.facingVector)
  })
}

fun updateMissiles(world: World, collisions: List<Collision>): List<Missile> {
  return world.missiles
      .map { updateMissile(world.bodyTable, world.characterTable, it, collisions, simulationDelta) }
}
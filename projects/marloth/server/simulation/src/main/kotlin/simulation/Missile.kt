package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.Table
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import physics.Body
import physics.Collision
import physics.commonShapes
import physics.overlaps
import simulation.data.missileBodyAttributes

data class Missile(
    override val id: Id,
    val owner: Id,
    val remainingDistance: Float
) : Entity

fun characterAttack(world: World, nextId: IdSource, character: Character, ability: Ability, direction: Vector3): Hand {
  val body = world.bodyTable[character.id]!!
  val id = nextId()
  return Hand(
      body = Body(
          id = id,
          position = body.position + direction * 0.5f + Vector3(0f, 0f, 1.4f),
          node = body.node,
          velocity = direction * ability.definition.maxSpeed,
          shape = commonShapes[EntityType.missile]!!,
          orientation = Quaternion(),
          attributes = missileBodyAttributes,
          gravity = false,
          perpetual = true
      ),
      missile = Missile(
          id = id,
          remainingDistance = ability.definition.range,
          owner = character.id
      )
  )
}

fun getBodyCollisions(bodies: Table<Body>, characterTable: Table<Character>, missiles: Collection<Missile>): List<Collision> {
  return missiles.flatMap { missile ->
    val body = bodies[missile.id]!!
    val owner = bodies[missile.owner]
    bodies.values.filter { it !== body && it !== owner }
        .filter { overlaps(it, body) }
        .map { hit ->
          Collision(
              first = missile.id,
              second = hit.id,
              hitPoint = Vector2(),
              directGap = 0f,
              travelingGap = 0f
          )
        }
  }
}

fun updateMissile(world: World, collisions: List<Collision>, delta: Float): (Missile) -> Missile = { missile ->
  val body = world.deck.bodies[missile.id]!!
  val offset = body.velocity * delta
  val hit = collisions.firstOrNull { it.first == missile.id }

  val remainingDistance = if (hit != null) {
//    if (world.table.characters[hit.second] != null) {
    0f
//    } else {
//      missile.remainingDistance - offset.length()
//    }
  } else {
    missile.remainingDistance - offset.length()
  }
  missile.copy(remainingDistance = remainingDistance)
}
//realm.walls.filter { isSolidWall(realm.faces[it.id]!!) }.any { hitsWall(it.edges[0].edge, position, body.radius!!)

fun isFinished(missile: Missile): Boolean =
    missile.remainingDistance <= 0

fun getNewMissiles(world: World, nextId: IdSource, activatedAbilities: List<ActivatedAbility>): Deck {
  return toDeck(activatedAbilities.map {
    val (character, ability) = it
    characterAttack(world, nextId, character, ability, character.facingVector)
  })
}

//fun updateMissiles(world: World, collisions: List<Collision>): List<Missile> {
//  return world.missiles
//      .map { updateMissile(world, it, collisions, simulationDelta) }
//}
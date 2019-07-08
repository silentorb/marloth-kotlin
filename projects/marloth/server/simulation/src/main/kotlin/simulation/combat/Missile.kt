package simulation.combat

//data class Missile(
//    override val id: Id,
//    val owner: Id,
//    val remainingDistance: Float
//) : Entity

//fun characterAttack(world: World, nextId: IdSource, character: Character, ability: Ability, direction: Vector3): IdHand {
//  val body = world.bodyTable[character.id]!!
//  val id = nextId()
//  return IdHand(id, Hand(
//      body = Body(
//          position = body.position + direction * 0.5f + Vector3(0f, 0f, 1.4f),
//          node = body.node,
//          velocity = direction * ability.definition.maxSpeed,
//          orientation = Quaternion()
//      ),
//      collisionShape = CollisionObject(shape = Sphere(0.2f)),
//      dynamicBody = DynamicBody(
//          gravity = false,
//          mass = 10f,
//          resistance = 4f
//      ),
//      missile = Missile(
//          id = id,
//          remainingDistance = ability.definition.range,
//          owner = character.id
//      )
//  ))
//}

//fun getBodyCollisions(bodies: Table<Body>, characterTable: Table<Character>, missiles: Collection<Missile>): List<Collision> {
//  return missiles.flatMap { missile ->
//    val body = bodies[missile.id]!!
//    bodies.values.filter {
//      val character = characterTable[it.id]
//      it.id != body.id && it.id != missile.owner && (character == null || character.isAlive)
//    }
//        .filter { overlaps(it, body) }
//        .map { hit ->
//          Collision(
//              first = missile.id,
//              second = hit.id,
//              hitPoint = Vector2(),
//              directGap = 0f,
//              travelingGap = 0f
//          )
//        }
//  }
//}

//fun updateMissile(world: World, collisions: List<Collision>, delta: Float): (Missile) -> Missile = { missile ->
//  val body = world.deck.bodies[missile.id]!!
//  val offset = body.velocity * delta
//  val hit = collisions.firstOrNull { it.first == missile.id }
//
//  val remainingDistance = if (hit != null) {
////    if (world.table.characters[hit.second] != null) {
//    0f
////    } else {
////      missile.remainingDistance - offset.length()
////    }
//  } else {
//    missile.remainingDistance - offset.length()
//  }
//  missile.copy(remainingDistance = remainingDistance)
//}
////realm.walls.filter { isSolidWall(realm.faces[it.id]!!) }.any { hitsWall(it.edges[0].edge, position, body.radius!!)
//
//fun isFinished(missile: Missile): Boolean =
//    missile.remainingDistance <= 0

//fun getNewMissiles(world: World, nextId: IdSource, activatedAbilities: List<ActivatedAbility>): Deck {
//  return toDeck(activatedAbilities.map {
//    val (character, ability) = it
//    characterAttack(world, nextId, character, ability, character.facingVector)
//  })
//}

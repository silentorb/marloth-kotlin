package simulation.physics

import silentorb.mythic.characters.*
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.*
import simulation.main.Deck
import simulation.main.World
import simulation.updating.simulationDelta

fun toPhysicsDeck(deck: Deck): PhysicsDeck =
    PhysicsDeck(
        bodies = deck.bodies,
        collisionObjects = deck.collisionShapes,
        dynamicBodies = deck.dynamicBodies
    )

fun updatePhysics(events: Events): (World) -> World = { world ->
  val deck = world.deck
  val physicsDeck = toPhysicsDeck(deck)
  val physicsWorld = PhysicsWorld(
      bulletState = world.bulletState,
      deck = physicsDeck
  )
  val linearForces = characterMovementsToImpulses(deck.bodies, deck.characterRigs, events)
      .plus(events.filterIsInstance<LinearImpulse>())

  val nextPhysicsWorld = updateBulletPhysics(linearForces)(physicsWorld)
  updateCharacterRigBulletBodies(nextPhysicsWorld.bulletState, deck.characterRigs)
  val nextDeck = nextPhysicsWorld.deck
  world.copy(
      bulletState = nextPhysicsWorld.bulletState,
      deck = world.deck.copy(
          bodies = nextDeck.bodies,
          collisionShapes = nextDeck.collisionObjects,
          dynamicBodies = nextDeck.dynamicBodies
      )
  )
}

fun newCharacterRigHand(deck: Deck): (Id) -> CharacterRigHand = { character ->
  CharacterRigHand(
      body = deck.bodies[character]!!,
      characterRig = deck.characterRigs[character]!!,
      collisionObject = deck.collisionShapes[character]!!
  )
}

//fun updateMarlothCharacterRigFacing(deck: Deck, commands: Commands, id: Id): (CharacterRig) -> CharacterRig = { characterRig ->
//  val destructible = deck.destructibles[id]!!
//  val character = deck.characters[id]!!
//  val isAlive = isAlive(destructible.health.value, deck.bodies[id]!!.position)
//  val justDied = !isAlive && character.isAlive
//
//  if (justDied) {
//    if (destructible.lastDamageSource != 0L && destructible.lastDamageSource != id && deck.players.containsKey(id)) {
//      val source = destructible.lastDamageSource
//      val killerBody = deck.bodies[source]
//      if (killerBody != null) {
//        val facingVector = (killerBody.position - deck.bodies[id]!!.position).normalize()
//        val lookAtAngle = getHorizontalLookAtAngle(facingVector)
//        characterRig.copy(
//            lookVelocity = Vector2(),
//            facingRotation = Vector3(0f, 0f, lookAtAngle)
//        )
//      } else
//        characterRig
//    } else
//      characterRig
//  } else {
//    updateCharacterRigFacing(commands, simulationDelta)(characterRig)
//  }
//}

fun updateMarlothCharacterRigActive(deck: Deck, id: Id): (CharacterRig) -> CharacterRig = { characterRig ->
  val character = deck.characters[id]
  if (character != null && character.isAlive != characterRig.isActive)
    characterRig.copy(
        isActive = character.isAlive
    )
  else
    characterRig
}

fun updateMarlothCharacterRig(bulletState: BulletState, deck: Deck,
                              events: Events): (Id, CharacterRig) -> CharacterRig {
  val allCommands = events
      .filterIsInstance<CharacterCommand>()

  val allMovements = events
      .filterIsInstance<CharacterRigMovement>()

  return { id, characterRig ->
    val commands = allCommands.filter { it.target == id }
    val movements = allMovements.filter { it.actor == id }

    pipe(
        updateMarlothCharacterRigActive(deck, id),
        updateCharacterRigGroundedDistance(bulletState, CollisionGroups.walkable, newCharacterRigHand(deck)(id)),
        updateCharacterRigFacing(commands, movements, simulationDelta)
    )(characterRig)
  }
}

package simulation.physics

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import silentorb.mythic.physics.*
import silentorb.mythic.happenings.Commands
import silentorb.mythic.characters.*
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import simulation.main.Deck
import simulation.main.World
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import simulation.entities.isAlive
import simulation.updating.simulationDelta

fun updatePhysics(events: Events): (World) -> World = { world ->
  val deck = world.deck
  val physicsDeck = PhysicsDeck(
      bodies = deck.bodies,
      collisionShapes = deck.collisionShapes,
      dynamicBodies = deck.dynamicBodies
  )
  val physicsWorld = PhysicsWorld(
      bulletState = world.bulletState,
      deck = physicsDeck
  )
  val commands = events.filterIsInstance<CharacterCommand>()
  val linearForces = allCharacterMovements(physicsDeck, deck.characterRigs, commands)
  val nextPhysicsWorld = updateBulletPhysics(linearForces)(physicsWorld)
  updateCharacterRigBulletBodies(nextPhysicsWorld.bulletState, deck.characterRigs)
  val nextDeck = nextPhysicsWorld.deck
  world.copy(
      bulletState = nextPhysicsWorld.bulletState,
      deck = world.deck.copy(
          bodies = nextDeck.bodies,
          collisionShapes = nextDeck.collisionShapes,
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

fun updateMarlothCharacterRigFacing(deck: Deck, commands: Commands, id: Id): (CharacterRig) -> CharacterRig = { characterRig ->
  val destructible = deck.destructibles[id]!!
  val character = deck.characters[id]!!
  val isAlive = isAlive(destructible.health.value)
  val justDied = !isAlive && character.isAlive

  if (justDied) {
    if (destructible.lastDamageSource != 0L) {
      val source = destructible.lastDamageSource
      val killerBody = deck.bodies[source]
      if (killerBody != null) {
        val facingVector = (killerBody.position - deck.bodies[id]!!.position).normalize()
        val lookAtAngle = getLookAtAngle(facingVector)
        characterRig.copy(
            lookVelocity = Vector2(),
            facingRotation = Vector3(0f, 0f, lookAtAngle)
        )
      } else
        characterRig
    } else
      characterRig
  } else {
    updateCharacterRigFacing(commands, simulationDelta)(characterRig)
  }
}

fun updateMarlothCharacterRig(bulletState: BulletState, deck: Deck,
                              events: Events): (Id, CharacterRig) -> CharacterRig = { id, characterRig ->
  val commands = events
      .filterIsInstance<CharacterCommand>()
      .filter { it.target == id }

  pipe(
      updateMarlothCharacterRigFacing(deck, commands, id),
      updateCharacterRigGroundedDistance(bulletState, newCharacterRigHand(deck)(id))
  )(characterRig)
}

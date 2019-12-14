package simulation.physics

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import silentorb.mythic.physics.*
import simulation.input.Commands
import simulation.main.Deck
import simulation.main.World
import silentorb.mythic.rigging.characters.CharacterRig
import silentorb.mythic.rigging.characters.updateCharacterRigFacing
import silentorb.mythic.rigging.characters.updateCharacterRigGroundedDistance
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import simulation.entities.isAlive
import simulation.misc.getLookAtAngle
import simulation.updating.simulationDelta

fun updatePhysics(linearForces: List<LinearImpulse>): (World) -> World = { world ->
  val deck = world.deck
  val physicsDeck = PhysicsDeck(
      bodies = deck.bodies,
      characterRigs = deck.characterRigs,
      collisionShapes = deck.collisionShapes,
      dynamicBodies = deck.dynamicBodies
  )
  val physicsWorld = PhysicsWorld(
      bulletState = world.bulletState,
      deck = physicsDeck
  )
  val nextPhysicsWorld = updateBulletPhysics(linearForces)(physicsWorld)
  val nextDeck = nextPhysicsWorld.deck
  world.copy(
      bulletState = nextPhysicsWorld.bulletState,
      deck = world.deck.copy(
          bodies = nextDeck.bodies,
          characterRigs = nextDeck.characterRigs,
          collisionShapes = nextDeck.collisionShapes,
          dynamicBodies = nextDeck.dynamicBodies
      )
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
                              allCommands: Commands): (Id, CharacterRig) -> CharacterRig = { id, characterRig ->
  val commands = allCommands.filter { it.target == id }
  pipe(
      updateMarlothCharacterRigFacing(deck, commands, id),
      updateCharacterRigGroundedDistance(bulletState, deck, id)
  )(characterRig)
}

package simulation.physics

import silentorb.mythic.physics.LinearImpulse
import silentorb.mythic.physics.PhysicsDeck
import silentorb.mythic.physics.PhysicsWorld
import silentorb.mythic.physics.updateBulletPhysics
import simulation.main.World

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

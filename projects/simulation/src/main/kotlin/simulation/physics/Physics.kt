package simulation.physics

import silentorb.mythic.characters.rigs.updateCharacterRigBulletBodies
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.*
import simulation.main.Deck
import simulation.main.World
import simulation.main.mergeDecks

fun toPhysicsDeck(deck: Deck): PhysicsDeck =
    PhysicsDeck(
        bodies = deck.bodies,
        collisionObjects = deck.collisionObjects,
        dynamicBodies = deck.dynamicBodies
    )

fun updatePhysics(events: Events): (World) -> World = { world ->
  val deck = world.deck
  val physicsDeck = toPhysicsDeck(mergeDecks(deck, world.realm.deck))
  val physicsWorld = PhysicsWorld(
      bulletState = world.bulletState,
      deck = physicsDeck,
      graph = world.staticGraph,
      meshShapes = world.definitions.meshShapeMap,
  )
  val linearForces = events.filterIsInstance<LinearImpulse>()
  val nextPhysicsWorld = updateBulletPhysics(linearForces)(physicsWorld)
  updateCharacterRigBulletBodies(nextPhysicsWorld.bulletState, deck.characterRigs, deck.bodies)
  val nextDeck = nextPhysicsWorld.deck
  world.copy(
      bulletState = nextPhysicsWorld.bulletState,
      deck = world.deck.copy(
          bodies = nextDeck.bodies,
          collisionObjects = nextDeck.collisionObjects,
          dynamicBodies = nextDeck.dynamicBodies
      )
  )
}

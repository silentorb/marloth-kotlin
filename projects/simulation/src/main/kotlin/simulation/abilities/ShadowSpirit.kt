package simulation.abilities

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import silentorb.mythic.characters.rigs.disableFreedomsBuff
import silentorb.mythic.characters.rigs.defaultCharacterHeight
import silentorb.mythic.characters.rigs.defaultCharacterRadius
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.ActionDefinition
import silentorb.mythic.physics.*
import silentorb.mythic.scenery.Capsule
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.Accessory
import simulation.characters.characterDynamicBody
import simulation.characters.commonCharacterElements
import simulation.characters.newAccessory
import simulation.entities.PlayerRigEvent
import simulation.main.Deck
import simulation.main.NewHand
import simulation.main.World
import simulation.physics.CollisionGroups

const val shadowSpiritBuff = "shadowSpiritBuff"

fun shadowSpiritPlacementOffset(dynamicsWorld: btDiscreteDynamicsWorld, collisionObject: CollisionObject, transform: Matrix): Vector3 {
  val shape = createCollisionShape(collisionObject.shape, Vector3.unit)
  val bulletBody = createBulletDynamicObject(transform, characterDynamicBody, shape, false)
  dynamicsWorld.addRigidBody(bulletBody, collisionObject.groups, collisionObject.mask)
//  dynamicsWorld.applySpeculativeContactRestitution = false
//  dynamicsWorld.stepSimulation(1f / 60f, 10)
  val newLocation = toVector3(bulletBody.worldTransform.getTranslation(com.badlogic.gdx.math.Vector3()))
  dynamicsWorld.removeRigidBody(bulletBody)
  bulletBody.release()
  return newLocation - transform.translation()
}

fun onShadowSpirit(world: World, actionDefinition: ActionDefinition, actor: Id): Events {
  val nextId = world.nextId.source()
  val shadowSpirit = nextId()
  val deck = world.deck
  val realBody = deck.bodies[actor]!!
  val realRig = deck.characterRigs[actor]!!
  val position = realBody.position + Vector3(0.4f, 0f, 0f)
      .transform(Matrix.identity.rotateZ(realRig.facingRotation.x))

  val angle = realRig.facingRotation.x
  val collisionObject = CollisionObject(
      shape = Capsule(defaultCharacterRadius, defaultCharacterHeight),
      groups = CollisionGroups.spirit,
      mask = CollisionGroups.spiritMask
  )

  return listOf(
      NewHand(
          id = shadowSpirit,
          components = commonCharacterElements(position, angle, 10f) + listOf(
              collisionObject,
          )
      ),
      NewHand(
          components = listOf(
              Accessory(
                  type = shadowSpiritBuff,
                  owner = actor,
              ),
              FloatTimer(10f)
          ),
      ),
      newAccessory(world.definitions, Actions.cancelShadowSpirit, shadowSpirit),
      PlayerRigEvent(
          player = actor,
          rig = shadowSpirit,
      )
  )
}

fun removeShadowSpirit(actor: Id, spiritActor: Id?): Events =
    listOfNotNull(
        if (spiritActor != null && spiritActor != actor)
          DeleteEntityEvent(spiritActor)
        else
          null,
        PlayerRigEvent(
            player = actor,
            rig = actor,
        )
    )

fun onCancelShadowSpirit(deck: Deck, spiritActor: Id): Events {
  val player = deck.players.entries.firstOrNull { it.value.rig == spiritActor }?.key
  return if (player == null)
    listOf()
  else
    deck.accessories
        .filter { it.value.owner == player && it.value.type == shadowSpiritBuff }
        .map { (key) -> DeleteEntityEvent(key) } +
        NewHand(
            components = listOf(
                Accessory(
                    type = disableFreedomsBuff,
                    owner = player,
                ),
                FloatTimer(0.2f)
            ),
        )
}

fun eventsFromShadowSpiritRemoval(previousDeck: Deck, world: World): Events {
  val removedBuffs = previousDeck.accessories.filter { (id, accessory) ->
    !world.deck.accessories.containsKey(id) && accessory.type == shadowSpiritBuff
  }

  return removedBuffs.flatMap { (id, accessory) ->
    val actor = accessory.owner!!
    val spiritRig = previousDeck.players[actor]?.rig
    removeShadowSpirit(actor, spiritRig)
  }
}

package simulation.abilities

import silentorb.mythic.characters.rigs.defaultCharacterHeight
import silentorb.mythic.characters.rigs.defaultCharacterRadius
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.performing.ActionDefinition
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.scenery.Capsule
import simulation.characters.commonCharacterElements
import simulation.entities.PlayerRigEvent
import simulation.main.NewHand
import simulation.main.World
import simulation.physics.CollisionGroups

fun onShadowSpirit(world: World, actionDefinition: ActionDefinition, actor: Id): Events {
  val nextId = world.nextId.source()
  val shadowSpirit = nextId()
  val deck = world.deck
  val realBody = deck.bodies[actor]!!
  val realRig = deck.characterRigs[actor]!!
  val position = realBody.position
  val angle = realRig.facingRotation.y

  val hand = NewHand(
      id = shadowSpirit,
      components = commonCharacterElements(position, angle) + listOf(
          CollisionObject(
              shape = Capsule(defaultCharacterRadius, defaultCharacterHeight),
              groups = CollisionGroups.spirit,
              mask = CollisionGroups.spiritMask
          ),
      )
  )

  return listOf(
      hand,
      PlayerRigEvent(
          player = actor,
          rig = shadowSpirit,
      )
  )
}

package simulation.intellect.design

import marloth.scenery.enums.AccessoryIdOld
import silentorb.mythic.ent.Id
import simulation.abilities.isEntangleImmune
import simulation.happenings.getActions
import simulation.main.World

fun actionsForTarget(world: World, actor: Id, target: Id): List<Id> {
  val definitions = world.definitions
  val deck = world.deck
  val actorBody = deck.bodies[actor]!!
  val targetBody = deck.bodies[target]!!
  val distance = actorBody.position.distance(targetBody.position)
  return getActions(definitions, deck.accessories, actor)
      .filter { (_, accessoryRecord) ->
        val action = definitions.actions[accessoryRecord.type]!!
        action.range >= distance &&
            (accessoryRecord.type != AccessoryIdOld.entangle || !isEntangleImmune(deck.accessories, target))
      }
      .keys
      .toList()
}

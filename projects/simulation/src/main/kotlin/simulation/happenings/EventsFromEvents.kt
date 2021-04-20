package simulation.happenings

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.allCharacterMovements
import silentorb.mythic.characters.rigs.characterMovementsToImpulses
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import simulation.abilities.eventsFromSleep
import simulation.accessorize.eventsFromItemPickup
import simulation.characters.newMoveSpeedTable
import simulation.combat.spatial.onAttack
import simulation.combat.toSpatialCombatWorld
import simulation.main.World
import simulation.physics.toPhysicsDeck

inline fun <reified T> mapEvents(crossinline transform: (T) -> Events): (Events) -> Events {
  return { events ->
    events
        .filterIsInstance<T>()
        .flatMap(transform)
  }
}

fun mapCommands(type: Any, transform: (Command, Id) -> Events): (Events) -> Events = { events ->
  events
      .filterIsInstance<Command>()
      .filter { it.type == type }
      .flatMap { command ->
        val actor = command.target as? Id
        if (actor == null)
          listOf()
        else {
          transform(command, actor)
        }
      }
}

fun eventsFromEvents(world: World, freedomTable: FreedomTable, events: Events): Events {
  val deck = world.deck
  val characterMovementEvents = allCharacterMovements(toPhysicsDeck(deck), deck.characterRigs, deck.thirdPersonRigs, events)
  val moveSpeedTable = newMoveSpeedTable(world.definitions, world.deck)
  return listOf(
      mapEvents(eventsFromTryAction(world, freedomTable)),
      mapEvents(onAttack(toSpatialCombatWorld(world))),
      mapCommands(CharacterCommands.take, eventsFromItemPickup(world)),
      mapCommands(CharacterCommands.sleep, eventsFromSleep(world)),
  )
      .fold(events) { a, b -> a + b(a) }
      .plus(characterMovementEvents)
      .plus(characterMovementsToImpulses(deck.bodies, deck.characterRigs, freedomTable, moveSpeedTable, characterMovementEvents))
}

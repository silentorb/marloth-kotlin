package simulation.happenings

import silentorb.mythic.characters.rigs.FreedomTable
import silentorb.mythic.characters.rigs.allCharacterMovements
import silentorb.mythic.characters.rigs.characterMovementsToImpulses
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import simulation.abilities.*
import simulation.accessorize.eventsFromItemPickup
import simulation.characters.ActivityEvents
import simulation.characters.eventsFromAbsenceStart
import simulation.characters.newMoveSpeedTable
import simulation.combat.spatial.onAttack
import simulation.combat.toSpatialCombatWorld
import simulation.entities.*
import simulation.interactions.harvestEvents
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

fun mapInteractions(type: String, transform: (Interaction, Id) -> Events): (Events) -> Events = { events ->
  events
      .filterIsInstance<Interaction>()
      .filter { it.type == type }
      .flatMap { interaction ->
        val actor = interaction.actor as? Id
        if (actor == null)
          listOf()
        else {
          transform(interaction, actor)
        }
      }
}

val interactionEventsMap = mapOf(
    InteractionBehaviors.take to ::eventsFromItemPickup,
    InteractionBehaviors.sleep to eventsFromAbsenceStart(sleepingEvent),
    InteractionBehaviors.devotion to eventsFromAbsenceStart(devotionEvent),
    InteractionBehaviors.open to ::eventsFromDoorOpening,
    InteractionBehaviors.close to ::eventsFromDoorClosing,
    InteractionBehaviors.harvest to ::harvestEvents,
)

fun mapInteractionEvents(world: World) =
    interactionEventsMap
        .map { (action, handler) ->
          mapInteractions(action, handler(world))
        }

fun eventsFromEvents(world: World, freedomTable: FreedomTable, events: Events): Events {
  val deck = world.deck
  val characterMovementEvents = allCharacterMovements(toPhysicsDeck(deck), deck.characterRigs, deck.thirdPersonRigs, events)
  val moveSpeedTable = newMoveSpeedTable(world.definitions, world.deck)
  return listOf(
      mapEvents(eventsFromTryAction(world, freedomTable)),
      mapEvents(onAttack(toSpatialCombatWorld(world))),
      mapCommands(sleepingEvent, eventsFromSleeping(world)),
      mapCommands(devotionEvent, eventsFromDevotion(world)),
  )
      .plus(mapInteractionEvents(world))
      .fold(events) { a, b -> a + b(a) }
      .plus(characterMovementEvents)
      .plus(characterMovementsToImpulses(deck.bodies, deck.characterRigs, freedomTable, moveSpeedTable, characterMovementEvents))
      .plus(eventsFromOpeningAndClosingTransitions(deck))
}

package simulation.entities

import marloth.scenery.enums.Sounds
import silentorb.mythic.audio.NewSound
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import simulation.main.Deck
import simulation.main.World
import simulation.physics.adjustOrientationCommand
import simulation.updating.simulationDelta

object DoorMode {
  val open = "open"
  val closed = "closed"
  val opening = "opening"
  val closing = "closing"
}

fun eventsFromDoorOpening(world: World): (Interaction, Id) -> Events = { interaction, actor ->
  val deck = world.deck
  val door = interaction.target
  val body = deck.bodies[door]
  if (body == null)
    listOf()
  else {
    listOf(
        Command(type = setPrimaryMode, target = door, value = DoorMode.opening),
        NewSound(
            type = Sounds.creakingDoor,
            position = body.position,
        ),
        NewSound(
            type = Sounds.doorLock,
            position = body.position,
        ),
    )
  }
}

fun eventsFromDoorClosing(world: World): (Interaction, Id) -> Events = { interaction, actor ->
  val deck = world.deck
  val door = interaction.target
  val body = deck.bodies[door]
  if (body == null)
    listOf()
  else {
    listOf(
        Command(type = setPrimaryMode, target = door, value = DoorMode.closing),
        NewSound(
            type = Sounds.creakingDoor,
            position = body.position,
        ),
    )
  }
}

val doorSpeed = 1.8f

fun eventsFromOpeningAndClosingTransitions(deck: Deck): Events {
  val doors = deck.primaryModes.filter { it.value.type == "door" }
  val openingUpdates = doors
      .filter { it.value.mode == DoorMode.opening }
      .keys
      .flatMap { door ->
        val body = deck.bodies[door]
        if (body == null || body.orientation.angleZ > Pi / 2f)
          listOf(
              Command(type = setPrimaryMode, target = door, value = DoorMode.open),
          )
        else
          listOf(
              Command(type = adjustOrientationCommand, target = door, value = Quaternion().rotateZ(Pi / 2f * doorSpeed * simulationDelta)),
          )
      }

  val closingUpdates = doors
      .filter { it.value.mode == DoorMode.closing }
      .keys
      .flatMap { door ->
        val body = deck.bodies[door]
        if (body == null || body.orientation.angleZ < 0.01f)
          listOfNotNull(
              Command(type = setPrimaryMode, target = door, value = DoorMode.closed),
              if (body != null) NewSound(
                  type = Sounds.doorLock,
                  position = body.position,
              )
              else
                null,
          )
        else
          listOf(
              Command(type = adjustOrientationCommand, target = door, value = -Quaternion().rotateZ(Pi / 2f * doorSpeed * simulationDelta)),
          )
      }

  return openingUpdates + closingUpdates
}

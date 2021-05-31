package simulation.physics

import silentorb.mythic.characters.rigs.CharacterRigMovement
import silentorb.mythic.characters.rigs.Freedom
import silentorb.mythic.characters.rigs.hasFreedom
import silentorb.mythic.characters.rigs.isGrounded
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Quaternion
import simulation.characters.isAliveOrNotACharacter
import simulation.entities.updateSpinnerRotation
import simulation.happenings.ReturnHome
import simulation.main.Deck
import simulation.movement.getFreedoms

const val adjustOrientationCommand = "adjustOrientation"

fun updateBodies(deck: Deck, events: Events, delta: Float): (Id, Body) -> Body {
  val returnHomeEvents = events.filterIsInstance<ReturnHome>()
  val movementEvents = events.filterIsInstance<CharacterRigMovement>()
  val allOrientationCommands = events
      .filterIsInstance<Command>()
      .filter { it.type == adjustOrientationCommand }

  return { id, body ->
//    val position = if (returnHomeEvents.any { it.target == id }) {
//      val home = grid.cells.entries
//          .first { (_, cell) -> cell.attributes.contains(CellAttribute.home) }
//      getCellPoint(home.key) + Vector3(0f, 0f, -2f)
//    } else
//      body.position
    val position = body.position

    val adjustedOrientation = allOrientationCommands
        .filter { it.target == id }
        .mapNotNull { it.value as? Quaternion }
        .fold(body.orientation) { a, q -> a * q }

    val orientation = updateSpinnerRotation(deck.spinners, delta, id, adjustedOrientation)

    val characterRig = deck.characterRigs[id]
    val velocity = if (
        characterRig != null && isGrounded(characterRig) &&
        isAliveOrNotACharacter(deck.characters, id) && (!hasFreedom(getFreedoms(deck)(id), Freedom.walking) ||
            movementEvents.none { it.actor == id })
        && body.velocity.length() > 0.00001f
//        && body.velocity.length() < 0.1f
    ) {
//      println(System.currentTimeMillis())
      body.velocity * 0.9f
//      Vector3.zero
    } else
      body.velocity

    body.copy(
        position = position,
        orientation = orientation,
        velocity = velocity
    )
  }
}

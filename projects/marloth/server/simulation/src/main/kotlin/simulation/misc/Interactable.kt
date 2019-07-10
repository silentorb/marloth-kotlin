package simulation.misc

import mythic.ent.Id
import mythic.spatial.Vector3
import scenery.Text
import simulation.main.Deck
import simulation.main.World

data class WidgetCommand(
    val text: Text
)

data class Interactable(
    val primaryCommand: WidgetCommand,
    val secondaryCommand: WidgetCommand? = null
)

private const val interactableMaxDistance = 5f
private const val interactableMaxRotation = 0.99f

fun getVisibleInteractable(deck: Deck, player: Id): Map.Entry<Id, Interactable>? {
  val playerBody = deck.bodies[player]!!
  val playerCharacter = deck.characters[player]!!
  val f = playerCharacter.facingVector
  val facingVector = Vector3(f.x, f.y, 0f)
  return deck.interactables.filter { (id, _) ->
    val body = deck.bodies[id]!!
    body.position.distance(playerBody.position) < interactableMaxDistance
        && (body.position - playerBody.position).normalize().dot(facingVector) > interactableMaxRotation
  }.entries.firstOrNull()
}

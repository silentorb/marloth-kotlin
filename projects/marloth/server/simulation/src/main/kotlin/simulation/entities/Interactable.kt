package simulation.entities

import mythic.ent.Id
import mythic.spatial.Vector3
import scenery.enums.ClientCommand
import scenery.enums.Text
import simulation.happenings.Action
import simulation.main.Deck

data class WidgetCommand(
    val text: Text,
    val action: Action? = null,
    val clientCommand: ClientCommand? = null
)

data class Interactable(
    val primaryCommand: WidgetCommand,
    val secondaryCommand: WidgetCommand? = null
)

private const val interactableMaxDistance = 5f
private const val interactableMaxRotation = 0.99f

typealias InteractableEntry = Map.Entry<Id, Interactable>

//fun getVisibleInteractable(deck: Deck, player: Id): InteractableEntry? {
//  val playerBody = deck.bodies[player]!!
//  val playerCharacter = deck.characters[player]!!
//  val f = playerCharacter.facingVector
//  val facingVector = Vector3(f.x, f.y, 0f)
//  return deck.interactables.filter { (id, _) ->
//    val body = deck.bodies[id]!!
//    val isInRange = body.position.distance(playerBody.position) < interactableMaxDistance
//    val isInFront = (body.position - playerBody.position).normalize().dot(facingVector) > interactableMaxRotation
//    isInRange && isInFront
//  }.entries.firstOrNull()
//}

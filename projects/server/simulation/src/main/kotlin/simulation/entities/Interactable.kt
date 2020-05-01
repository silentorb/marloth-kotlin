package simulation.entities

import silentorb.mythic.ent.Id
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import silentorb.mythic.happenings.EventTrigger

data class WidgetCommand(
    val text: Text,
    val action: EventTrigger? = null,
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
//  val playerCharacter = deck.silentorb.mythic.characters[player]!!
//  val f = playerCharacter.facingVector
//  val facingVector = Vector3(f.x, f.y, 0f)
//  return deck.interactables.filter { (id, _) ->
//    val body = deck.bodies[id]!!
//    val isInRange = body.position.distance(playerBody.position) < interactableMaxDistance
//    val isInFront = (body.position - playerBody.position).normalize().dot(facingVector) > interactableMaxRotation
//    isInRange && isInFront
//  }.entries.firstOrNull()
//}

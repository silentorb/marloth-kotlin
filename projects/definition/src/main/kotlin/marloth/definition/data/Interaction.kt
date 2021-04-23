package marloth.definition.data

import marloth.scenery.enums.DevText
import simulation.entities.Interactable
import simulation.entities.Interactions
import simulation.entities.WidgetCommand

val interactionLabels = mapOf(
    Interactions.sleep to "Sleep",
    Interactions.take to "Take",
)

fun newInteraction(type: String) =
    Interactable(
        primaryCommand = WidgetCommand(
            text = DevText(interactionLabels[type]!!),
            commandType = type,
        )
    )

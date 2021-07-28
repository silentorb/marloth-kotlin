package marloth.definition.data

import simulation.entities.InteractionActions

val interactionLabels = mapOf(
    InteractionActions.sleep to "Sleep",
    InteractionActions.take to "Take",
)

//fun newInteraction(type: String) =
//    Interactable(
//        primaryCommand = WidgetCommand(
//            text = DevText(interactionLabels[type]!!),
//            commandType = type,
//        )
//    )

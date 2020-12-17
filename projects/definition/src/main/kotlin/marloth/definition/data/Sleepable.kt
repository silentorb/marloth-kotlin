package marloth.definition.data

import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.DevText
import simulation.entities.Interactable
import simulation.entities.WidgetCommand

fun newSleepable(): List<Any> = listOf(
    Interactable(
        primaryCommand = WidgetCommand(
            text = DevText("Sleep"),
            commandType = CharacterCommands.sleep
        )
    )
)

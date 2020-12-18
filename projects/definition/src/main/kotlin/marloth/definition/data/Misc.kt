package marloth.definition.data

import marloth.scenery.enums.CharacterCommands
import marloth.scenery.enums.DevText
import simulation.entities.Interactable
import simulation.entities.WidgetCommand
import simulation.main.NewHand
import simulation.misc.GameAttributes

fun miscellaneousDefinitions(): Map<String, NewHand> =
    mapOf(
        GameAttributes.sleepable to
            NewHand(
                components = listOf(
                    Interactable(
                        primaryCommand = WidgetCommand(
                            text = DevText("Sleep"),
                            commandType = CharacterCommands.sleep
                        )
                    )
                )
            )
    )

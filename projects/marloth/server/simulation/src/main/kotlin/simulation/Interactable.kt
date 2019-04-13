package simulation

import scenery.Text

data class WidgetCommand(
    val text: Text
)

data class Interactable(
    val primaryCommand: WidgetCommand,
    val secondaryCommand: WidgetCommand? = null
)
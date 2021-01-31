package marloth.definition.misc

import generation.general.Direction
import generation.general.directionNames
import silentorb.mythic.editing.PropertyDefinition
import silentorb.mythic.editing.PropertyDefinitions
import silentorb.mythic.editing.commonPropertyDefinitions
import silentorb.mythic.editing.components.dropDownWidget
import simulation.misc.MarlothProperties

fun marlothEditorPropertyDefinitions(sides: List<String> = blockSides): PropertyDefinitions = mapOf(
    MarlothProperties.mine to PropertyDefinition(
        displayName = "Mine",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.other to PropertyDefinition(
        displayName = "Other",
        widget = dropDownWidget { sides },
        defaultValue = { sides.firstOrNull() },
    ),
    MarlothProperties.direction to PropertyDefinition(
        displayName = "Direction",
        widget = dropDownWidget { directionNames },
        defaultValue = { Direction.east.name },
    ),
) + commonPropertyDefinitions()

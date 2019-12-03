package marloth.definition.data

import scenery.enums.AccessoryId
import scenery.enums.Text
import simulation.entities.AccessoryDefinition
import simulation.entities.AccessoryName
import simulation.entities.ActionDefinition

data class ActionAccessory(
    val accessory: AccessoryDefinition,
    val action: ActionDefinition
)

fun staticActionAccessories(): Map<AccessoryName, ActionAccessory> = mapOf(
    AccessoryId.pistol.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_pistol
        ),
        action = ActionDefinition(
            cooldown = 1f,
            range = 10f
        )
    )
)

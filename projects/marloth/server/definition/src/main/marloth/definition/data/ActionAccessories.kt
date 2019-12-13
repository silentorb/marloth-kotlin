package marloth.definition.data

import marloth.scenery.AnimationId
import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
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
            cooldown = 2f,
            range = 10f,
            animation = AnimationId.shootPistol.name
        )
    )
)
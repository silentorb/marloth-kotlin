package marloth.definition.data

import marloth.scenery.AnimationId
import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
import silentorb.mythic.combat.DamageDefinition
import marloth.scenery.enums.DamageTypes
import silentorb.mythic.combat.WeaponDefinition
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import simulation.entities.ActionDefinition

data class ActionAccessory(
    val accessory: AccessoryDefinition,
    val action: ActionDefinition,
    val weapon: WeaponDefinition? = null
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
        ),
        weapon = WeaponDefinition(
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            )
        )
    )
)

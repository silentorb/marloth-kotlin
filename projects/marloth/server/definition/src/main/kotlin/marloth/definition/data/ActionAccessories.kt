package marloth.definition.data

import marloth.scenery.enums.*
import silentorb.mythic.combat.general.DamageDefinition
import silentorb.mythic.combat.general.WeaponDefinition
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.general.AttackMethod
import silentorb.mythic.performing.ActionDefinition

data class ActionAccessory(
    val accessory: AccessoryDefinition,
    val action: ActionDefinition,
    val weapon: WeaponDefinition? = null
)

fun staticActionAccessories(): Map<AccessoryName, ActionAccessory> = mapOf(
    AccessoryId.pistol.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_pistol,
            mesh = MeshId.pistol.name
        ),
        action = ActionDefinition(
            cooldown = 2f,
            range = 10f,
            animation = AnimationId.shootPistol.name
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = Sounds.pistolFire.name
        )
    )
)

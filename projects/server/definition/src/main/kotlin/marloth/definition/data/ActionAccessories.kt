package marloth.definition.data

import marloth.definition.enums.MeshId
import marloth.scenery.enums.*
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.general.AttackMethod
import silentorb.mythic.combat.general.DamageDefinition
import silentorb.mythic.combat.general.WeaponDefinition
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
    ),

    AccessoryId.grenadeLauncher.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_grenadeLauncher,
            mesh = MeshId.grenadeLauncher.name
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 20f,
            animation = AnimationId.shootPistol.name
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 2f,
            velocity = 20f,
            damageFalloff = 1f,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = Sounds.pistolFire.name
        )
    ),

    AccessoryId.rocketLauncher.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_rocketLauncher,
            mesh = MeshId.grenadeLauncher.name
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 20f,
            animation = AnimationId.shootPistol.name
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 2f,
            velocity = 20f,
            damageFalloff = 1f,
            missileMesh = MeshId.missile.name,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = Sounds.pistolFire.name
        )
    ),

    AccessoryId.claws.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.unnamed,
            mesh = null
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 3f,
            animation = AnimationId.clawAttack.name
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.melee,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = Sounds.pistolFire.name
        )
    ),

    AccessoryId.shotgun.name to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.unnamed,
            mesh = MeshId.shotgun.name
        ),
        action = ActionDefinition(
            cooldown = 0.2f,
            range = 10f,
            animation = AnimationId.shootPistol.name
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            impulse = 1000f,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 30
                )
            ),
            sound = Sounds.pistolFire.name
        )
    )

)

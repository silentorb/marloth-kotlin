package marloth.definition.data

import marloth.scenery.enums.MeshId
import marloth.scenery.enums.*
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import simulation.combat.general.AttackMethod
import simulation.combat.general.DamageDefinition
import simulation.combat.general.WeaponDefinition
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
            equippedMesh = MeshId.pistol.name
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
            equippedMesh = MeshId.grenadeLauncher.name
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
            equippedMesh = MeshId.grenadeLauncher.name
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
            equippedMesh = null
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
            equippedMesh = MeshId.shotgun.name
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

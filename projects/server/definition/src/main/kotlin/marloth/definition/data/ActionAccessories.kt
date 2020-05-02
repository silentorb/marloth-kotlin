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

    AccessoryId.entangle to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_pistol,
            equippedMesh = MeshId.pistol
        ),
        action = ActionDefinition(
            cooldown = 2f,
            range = 10f,
            animation = AnimationId.shootPistol
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryId.mobility to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_mobility
        ),
        action = ActionDefinition(
            cooldown = 2f,
            duration = 3f
        )
    ),

    AccessoryId.pistol to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_pistol,
            equippedMesh = MeshId.pistol
        ),
        action = ActionDefinition(
            cooldown = 2f,
            range = 10f,
            animation = AnimationId.shootPistol
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryId.grenadeLauncher to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_grenadeLauncher,
            equippedMesh = MeshId.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 20f,
            animation = AnimationId.shootPistol
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 1f,
            velocity = 20f,
            damageFalloff = 1f,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryId.rocketLauncher to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.id_rocketLauncher,
            equippedMesh = MeshId.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 25f,
            animation = AnimationId.shootPistol
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 2f,
            velocity = 30f,
            damageFalloff = 1f,
            missileMesh = MeshId.missile,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryId.claws to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.unnamed,
            equippedMesh = null
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 3f,
            animation = AnimationId.clawAttack
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.melee,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical.name,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryId.shotgun to ActionAccessory(
        accessory = AccessoryDefinition(
            name = Text.unnamed,
            equippedMesh = MeshId.shotgun
        ),
        action = ActionDefinition(
            cooldown = 0.2f,
            range = 10f,
            animation = AnimationId.shootPistol
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
            sound = SoundId.pistolFire
        )
    )

)

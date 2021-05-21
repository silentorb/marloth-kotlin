package marloth.definition.data

import marloth.scenery.enums.*
import silentorb.mythic.performing.ActionCost
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName
import silentorb.mythic.performing.ActionDefinition
import simulation.abilities.Actions
import simulation.accessorize.Nutrient
import simulation.characters.EquipmentSlot
import simulation.combat.general.AttackMethod
import simulation.combat.general.DamageDefinition
import simulation.combat.general.ResourceTypes
import simulation.combat.general.WeaponDefinition

data class ActionAccessory(
    val accessory: AccessoryDefinition,
    val action: ActionDefinition,
    val weapon: WeaponDefinition? = null
)

fun rocketLauncher() =
    ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_rocketLauncher,
            equippedMesh = MeshId.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 1.5f,
            range = 25f,
            equipmentSlot = EquipmentSlot.attack,
            cost = ActionCost(
                type = ResourceTypes.energy,
                amount = 5
            )
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 2f,
            velocity = 20f,
            damageFalloff = 1f,
            missileMesh = MeshId.missile,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    )

fun apple() =
    ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Apple"),
            isConsumable = true,
            components = listOf(
                Nutrient(
                    value = 30
                )
            )
        ),
        action = ActionDefinition(
            equipmentSlot = EquipmentSlot.utility
        ),
    )

fun spiritRocketLauncher(): ActionAccessory {
  val accessory = rocketLauncher()
  return accessory.copy(
      action = accessory.action.copy(
          cooldown = 2.4f,
          animation = AnimationId.shootPistol
      )
  )
}

fun actionAccessories(): Map<AccessoryName, ActionAccessory> = mapOf(

    AccessoryIdOld.dash to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_dash,
            maxLevel = 3
        ),
        action = ActionDefinition(
            type = Actions.dash,
            cooldown = 4f,
            duration = 1.3f,
            equipmentSlot = EquipmentSlot.mobility
        )
    ),

    AccessoryIdOld.entangle to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_entangle,
            maxLevel = 3
        ),
        action = ActionDefinition(
            type = Actions.entangle,
            cooldown = 4f,
            range = 20f,
            animation = AnimationId.cast,
            equipmentSlot = EquipmentSlot.utility
        )
    ),

    AccessoryIdOld.grenadeLauncher to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_grenadeLauncher,
            equippedMesh = MeshId.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 3f,
            range = 15f,
            equipmentSlot = EquipmentSlot.attack,
            cost = ActionCost(
                type = ResourceTypes.energy,
                amount = 5
            )
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.lobbed,
            damageRadius = 3.5f,
            velocity = 20f,
            damageFalloff = 0.8f,
            missileMesh = MeshId.missile,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 80
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryIdOld.mobility to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_mobility
        ),
        action = ActionDefinition(
            cooldown = 3f,
            duration = 2f,
            equipmentSlot = EquipmentSlot.automatic
        )
    ),

    AccessoryIdOld.pistol to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_pistol,
            equippedMesh = MeshId.pistol
        ),
        action = ActionDefinition(
            cooldown = 2f,
            range = 10f,
            animation = AnimationId.shootPistol,
            equipmentSlot = EquipmentSlot.attack
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 70
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    AccessoryIdOld.rocketLauncher to rocketLauncher(),
    AccessoryIdOld.spiritRocketLauncher to spiritRocketLauncher(),
    Accessories.apple to apple(),

    AccessoryIdOld.claws to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.unnamed,
            equippedMesh = null
        ),
        action = ActionDefinition(
            cooldown = 1f,
            range = 3.5f,
            animation = AnimationId.clawAttack,
            equipmentSlot = EquipmentSlot.attack
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.melee,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 80
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    Accessories.shadowSpirit to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Shadow Spirit"),
        ),
        action = ActionDefinition(
            type = Actions.shadowSpirit,
            cooldown = 2f,
            duration = 10f,
            equipmentSlot = EquipmentSlot.mobility,
            cost = ActionCost(
                type = ResourceTypes.energy,
                amount = 2
            )
        )
    ),

    Accessories.cancelShadowSpirit to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Return"),
        ),
        action = ActionDefinition(
            type = Actions.cancelShadowSpirit,
            cooldown = 0f,
            equipmentSlot = EquipmentSlot.mobility
        )
    ),

    AccessoryIdOld.shotgun to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.unnamed,
            equippedMesh = MeshId.shotgun
        ),
        action = ActionDefinition(
            cooldown = 0.2f,
            range = 10f,
            animation = AnimationId.shootPistol,
            equipmentSlot = EquipmentSlot.attack
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            impulse = 1000f,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 30
                )
            ),
            sound = SoundId.pistolFire
        )
    ),

    )

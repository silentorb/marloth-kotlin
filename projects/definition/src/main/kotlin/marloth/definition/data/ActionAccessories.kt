package marloth.definition.data

import marloth.definition.misc.multiLevelActionAccessory
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

typealias ActionAccessoryMap = Map<AccessoryName, ActionAccessory>

fun rocketLauncher(level: Int = 1) =
    ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_rocketLauncher,
            equippedMesh = Meshes.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 1.5f,
            range = 25f,
            equipmentSlot = EquipmentSlot.attack,
            cost = ActionCost(
                type = ResourceTypes.energy,
                amount = 4 + level
            )
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.missile,
            damageRadius = 2f,
            velocity = 20f,
            damageFalloff = 1f,
            missileMesh = Meshes.missile,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 40 + 30 * level
                )
            ),
            sound = Sounds.pistolFire
        )
    )

fun spiritRocketLauncher(level: Int): ActionAccessory {
  val accessory = rocketLauncher(level)
  return accessory.copy(
      action = accessory.action.copy(
          cooldown = 2.4f,
          animation = AnimationId.shootPistol
      )
  )
}

fun claws(level: Int) =
    ActionAccessory(
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
                    amount = 50 + 30 * level
                )
            ),
            sound = Sounds.pistolFire
        )
    )

fun pistol(level: Int) =
    ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_pistol,
            equippedMesh = Meshes.pistol
        ),
        action = ActionDefinition(
            cooldown = 2f,
            range = 20f,
            animation = AnimationId.shootPistol,
            equipmentSlot = EquipmentSlot.attack
        ),
        weapon = WeaponDefinition(
            attackMethod = AttackMethod.raycast,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 40 + 30 * level
                )
            ),
            sound = Sounds.pistolFire
        )
    )

fun actionAccessories(): ActionAccessoryMap = mapOf(

    AccessoryIdOld.grenadeLauncher to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_grenadeLauncher,
            equippedMesh = Meshes.grenadeLauncher
        ),
        action = ActionDefinition(
            cooldown = 2f,
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
            missileMesh = Meshes.missile,
            damages = listOf(
                DamageDefinition(
                    type = DamageTypes.physical,
                    amount = 80
                )
            ),
            sound = Sounds.pistolFire
        )
    ),

    AccessoryIdOld.mobility to ActionAccessory(
        accessory = AccessoryDefinition(
            name = TextId.id_mobility
        ),
        action = ActionDefinition(
            cooldown = 3f,
            duration = 2f,
            equipmentSlot = EquipmentSlot.automatic,
        )
    ),

    AccessoryIdOld.rocketLauncher to rocketLauncher(),

    Accessories.apple to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Small Apple"),
            isConsumable = true,
            pickupSound = Sounds.takePlant,
            components = listOf(
                Nutrient(
                    value = 30
                )
            )
        ),
        action = ActionDefinition(
            equipmentSlot = EquipmentSlot.utility
        ),
    ),

    Accessories.apple2 to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Large Apple"),
            isConsumable = true,
            pickupSound = Sounds.takePlant,
            components = listOf(
                Nutrient(
                    value = 45
                )
            )
        ),
        action = ActionDefinition(
            equipmentSlot = EquipmentSlot.utility
        ),
    ),

    Accessories.apple3 to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Huge Apple"),
            isConsumable = true,
            pickupSound = Sounds.takePlant,
            components = listOf(
                Nutrient(
                    value = 60
                )
            )
        ),
        action = ActionDefinition(
            equipmentSlot = EquipmentSlot.utility
        ),
    ),

    Accessories.berries to ActionAccessory(
        accessory = AccessoryDefinition(
            name = DevText("Berries"),
            isConsumable = true,
            pickupSound = Sounds.takePlant,
            components = listOf(
                Nutrient(
                    value = 30
                ),
            ),
        ),
        action = ActionDefinition(
            equipmentSlot = EquipmentSlot.utility
        ),
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
            name = DevText("Shotgun"),
            equippedMesh = Meshes.shotgun
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
            sound = Sounds.pistolFire
        )
    ),

    ) +
    multiLevelActionAccessory(AccessoryIdOld.spiritRocketLauncher, 3, ::spiritRocketLauncher) +
    multiLevelActionAccessory(AccessoryIdOld.pistol, 3, ::pistol) +
    multiLevelActionAccessory(AccessoryIdOld.claws, 3, ::claws) +
    multiLevelActionAccessory(AccessoryIdOld.entangle, 3) {
      ActionAccessory(
          accessory = AccessoryDefinition(
              name = TextId.id_entangle,
          ),
          action = ActionDefinition(
              type = Actions.entangle,
              cooldown = 4f,
              range = 20f,
              animation = AnimationId.cast,
              equipmentSlot = EquipmentSlot.utility
          )
      )
    } +
    multiLevelActionAccessory(AccessoryIdOld.dash, 3) {
      ActionAccessory(
          accessory = AccessoryDefinition(
              name = TextId.id_dash,
          ),
          action = ActionDefinition(
              type = Actions.dash,
              cooldown = 4f,
              duration = 1.3f,
              equipmentSlot = EquipmentSlot.mobility
          )
      )
    }

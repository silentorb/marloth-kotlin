package junk_simulation.data

import junk_simulation.*

class Abilities {
  companion object {
    val Wait = AbilityType(
        name = "Wait",
        action = Actions.nothing,
        info = "Skips a turn",
        target = AbilityTarget.global,
        selectable = false
    )
    val Punch = AbilityType(
        name = "Punch",
        action = Actions.attack,
        arguments = listOf(
            0
        ),
        target = AbilityTarget.element,
        aiTarget = AbilityTarget.element,
        info = "Deals Damage",
        effect = Effects.missileAttack,
        cooldown = 0
    )
    val Shotgun = AbilityType(
        name = "Shotgun",
        action = Actions.attack,
        arguments = listOf(
            2
        ),
        cost = mapOf(
            Element.robot to 1
        ),
        target = AbilityTarget.element,
        aiTarget = AbilityTarget.element,
        info = "Deals Damage",
        effect = Effects.missileAttack,
        cooldown = 1
    )
    val Lightning = AbilityType(
        name = "Lightning",
        action = Actions.attack,
        arguments = listOf(
            1
        ),
        cost = mapOf(
            Element.ethereal to 1
        ),
        target = AbilityTarget.element,
        aiTarget = AbilityTarget.element,
        info = "Deals Damage. Ignores armor.",
        effect = Effects.missileAttack,
        cooldown = 1
    )
    val Regeneration = AbilityType(
        name = "Regeneration",
        action = Actions.heal_self,
        arguments = listOf(
            0
        ),
        cost = mapOf(
            Element.plant to 0
        ),
        trigger = Triggers.update,
        info = "Restores 1 of each element each round."
    )
    val Circumvent = AbilityType(
        name = "Circumvent",
        cost = mapOf(
            Element.robot to 0
        ),
        selectable = false,
        info = "Allows a creature to bypass your summoned units"
    )
    val multipleAttack = AbilityType(
        name = "Multiple Attack",
        cost = mapOf(
            Element.robot to 0
        ),
        selectable = false,
        info = "Allows a creature to attack twice each round if able"
    )
    val missileLauncher = AbilityType(
    name = "Missile Launcher",
    action = Actions.attack,
    arguments = listOf(
    3
    ),
    cost = mapOf(
    Element.robot to 2
    ),
    target = AbilityTarget.element,
    aiTarget = AbilityTarget.element,
    info = "Has a chance to permanently damage a creature's element",
    effect = Effects.missileAttack,
    selectable = false,
    cooldown = 1
    )
    val Poisoned = AbilityType(
        name = "Poisoned",
        action = Actions.poisoned,
        selectable = false,
        trigger = Triggers.update,
        info = "This creature is taking 1 point of damage per round and has any regeneration reduced"
    )
    val Poison = AbilityType(
        name = "Poison",
        action = Actions.poison,
        arguments = listOf(
            3
        ),
        cost = mapOf(
            Element.plant to 1
        ),
        target = AbilityTarget.creature,
        aiTarget = AbilityTarget.enemy,
        effect = Effects.missileAttack,
        cooldown = 1,
        info = "Poisoned creatures take 1 point of damage for each element per round and have any regeneration abilities reduced. Targets a creature, not its elements"
    )
    val Armor = AbilityType(
        name = "Armor",
        cost = mapOf(
            Element.robot to 0
        ),
        info = "Reduces the amount of damage received from an attack by 1 point per level. Has no effect on meta attacks and poison"
    )
    val Small = AbilityType(
        name = "Small",
        selectable = false,
        info = "This creature cannot take more than %level% damage per attack"
    )
    val Thorns = AbilityType(
        name = "Thorns",
        cost = mapOf(
            Element.plant to 1
        ),
        info = "Reflects a percentage of damage received back onto the attacker"
    )
    val Flux = AbilityType(
        name = "Flux",
        action = Actions.flux,
        cost = mapOf(
            Element.ethereal to 1
        ),
        target = AbilityTarget.global,
        cooldown = 2,
        info = "Restores a certain amount to each of your elements. Can increase an element beyond its maximum"
    )
    val Recycle = AbilityType(
        name = "Recycle",
        cost = mapOf(
            Element.robot to 0
        ),
        info = "Regain a percentage of a killed enemy's elements. A greater yield for element types you share"
    )
    val Sacrifice = AbilityType(
        name = "Sacrifice",
        action = Actions.sacrifice,
        trigger = Triggers.die,
        selectable = false,
        info = "When this creature dies, the creature who summoned it will be healed half of this creature's maximum element amount"
    )
    val Upkeep = AbilityType(
        name = "Upkeep",
        action = Actions.upkeep,
        trigger = Triggers.update,
        selectable = false,
        info = ""
    )
    val Grenade = AbilityType(
        name = "Grenade",
        action = Actions.attack,
        arguments = listOf(
            2
        ),
        cost = mapOf(
            Element.robot to 1
        ),
        target = AbilityTarget.global,
        effect = Effects.mass_damage,
        cooldown = 3,
        info = "Deals damage to each creature in play except for the Grenade thrower."
    )
    val wallOfWind = AbilityType(
        name = "Wall of Wind",
        action = Actions.summon,
        arguments = listOf(
            "Wall of Wind",
            4
        ),
        cost = mapOf(
            Element.ethereal to 2
        ),
        target = AbilityTarget.global,
        cooldown = 2,
        info = "Summons a vaporous wall. Upkeep = 1 Ethereal element per round"
    )
    val constructMinion = AbilityType(
        name = "Construct Minion",
        action = Actions.summon,
        arguments = listOf(
            "Construct Minion",
            4
        ),
        cost = mapOf(
            Element.robot to 2
        ),
        target = AbilityTarget.global,
        cooldown = 2,
        info = "Summons a metal minion. Upkeep = 1 Robot element per round"
    )
    val summonSapper = AbilityType(
        name = "Summon Sapper",
        action = Actions.summon,
        arguments = listOf(
            "Summon Sapper",
            4
        ),
        cost = mapOf(
            Element.plant to 1
        ),
        target = AbilityTarget.global,
        selectable = false,
        cooldown = 1,
        info = "Summons a sapper. Upkeep = 1 Plant every other round"
    )
  }
}

val abilityLibrary = Abilities::class.java.kotlin.members.toList()
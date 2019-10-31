package compuquest_simulation.data

import compuquest_simulation.CreatureCategory
import compuquest_simulation.CreatureType
import compuquest_simulation.Element
import compuquest_simulation.reflectProperties

class Creatures {
  companion object {
    val player = CreatureType(name = "Hero",
        category = CreatureCategory.player,
        life = 8
    )
    val sporeColony = CreatureType(
        name = "Spore Colony",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 3
        ),
        life = 3,
        abilities = listOf(
            Abilities.punch
        ),
        level = 1,
        frequency = 5
    )
    val ghost = CreatureType(
        name = "Ghost",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.ethereal to 5
        ),
        life = 5,
        abilities = listOf(
            Abilities.lightning
        ),
        level = 2,
        frequency = 4
    )
    val droid = CreatureType(
        name = "Droid",
        category = CreatureCategory.enemy,
        life = 5,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Abilities.shotgun,
            Abilities.circumvent
        ),
        level = 2,
        frequency = 4
    )
    val bombCaster = CreatureType(
        name = "Bomb Caster",
        category = CreatureCategory.enemy,
        life = 5,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Abilities.grenade
        ),
        level = 2,
        frequency = 4
    )
    val staticNettle = CreatureType(
        name = "Static Nettle",
        category = CreatureCategory.enemy,
        life = 18,
        elements = mapOf(
            Element.ethereal to 18,
            Element.plant to 15
        ),
        abilities = listOf(
            Abilities.lightning,
            Abilities.thorns
        ),
        level = 3,
        frequency = 3
    )
    val venomousVine = CreatureType(
        name = "Venomous Vine",
        category = CreatureCategory.enemy,
        life = 8,
        elements = mapOf(
            Element.plant to 8
        ),
        abilities = listOf(
            Abilities.poison,
            Abilities.regeneration
        ),
        level = 2,
        frequency = 3
    )
    val obsidianDwarf = CreatureType(
        name = "Obsidian Dwarf",
        category = CreatureCategory.enemy,
        life = 10,
        elements = mapOf(
            Element.robot to 10
        ),
        abilities = listOf(
            Abilities.shotgun,
            Abilities.small
        ),
        level = 4,
        frequency = 3
    )
    val cyberAngel = CreatureType(
        name = "Cyber Angel",
        category = CreatureCategory.enemy,
        life = 12,
        elements = mapOf(
            Element.ethereal to 12,
            Element.robot to 12
        ),
        abilities = listOf(
            Abilities.lightning,
            Abilities.shotgun
        ),
        level = 3,
        frequency = 3
    )
    val mossTank = CreatureType(
        name = "Moss Tank",
        category = CreatureCategory.enemy,
        life = 12,
        elements = mapOf(
            Element.plant to 12,
            Element.robot to 12
        ),
        abilities = listOf(
            Abilities.grenade,
            Abilities.punch,
            Abilities.armor
        ),
        level = 4,
        frequency = 3
    )
    val cyberEnt = CreatureType(
        name = "Cyber Ent",
        category = CreatureCategory.enemy,
        life = 18,
        elements = mapOf(
            Element.plant to 12,
            Element.robot to 18
        ),
        abilities = listOf(
            Abilities.missileLauncher
        ),
        level = 5,
        frequency = 2
    )
    val wallofWind = CreatureType(
        name = "Wall of Wind",
        category = CreatureCategory.ally,
        life = 4,
        elements = mapOf(
            Element.ethereal to 2
        ),
        abilities = listOf(
            Abilities.sacrifice
        ),
        level = 1
    )
    val wallofWeed = CreatureType(
        name = "Wall of Weed",
        category = CreatureCategory.ally,
        life = 6,
        elements = mapOf(
            Element.plant to 6
        ),
        abilities = listOf(
            Abilities.sacrifice,
            Abilities.regeneration
        ),
        level = 1
    )
    val golem = CreatureType(
        name = "Golem",
        category = CreatureCategory.ally,
        life = 3,
        elements = mapOf(
            Element.robot to 3
        ),
        abilities = listOf(
            Abilities.punch,
            Abilities.sacrifice
        ),
        level = 1
    )
    val hive = CreatureType(
        name = "Hive",
        category = CreatureCategory.enemy,
        life = 20,
        elements = mapOf(
            Element.plant to 20
        ),
        abilities = listOf(
            Abilities.summonSapper
        ),
        level = 4,
        frequency = 3
    )
    val assassin = CreatureType(
        name = "Assassin",
        category = CreatureCategory.enemy,
        life = 14,
        elements = mapOf(
            Element.plant to 14,
            Element.robot to 11
        ),
        abilities = listOf(
            Abilities.poison,
            Abilities.circumvent
        ),
        level = 4,
        frequency = 1
    )
    val underLord = CreatureType(
        name = "UnderLord",
        category = CreatureCategory.enemy,
        life = 22,
        elements = mapOf(
            Element.plant to 20,
            Element.robot to 22,
            Element.ethereal to 22
        ),
        abilities = listOf(
            Abilities.lightning,
            Abilities.shotgun,
            Abilities.poison,
            Abilities.regeneration,
            Abilities.multipleAttack
        ),
        level = 6,
        frequency = 2
    )
  }
}

val creatureLibrary: List<CreatureType> = reflectProperties(Creatures.Companion)

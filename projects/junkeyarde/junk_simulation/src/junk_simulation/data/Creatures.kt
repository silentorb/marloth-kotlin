package junk_simulation.data

import junk_simulation.CreatureCategory
import junk_simulation.CreatureType
import junk_simulation.Element
import junk_simulation.reflectProperties

class Creatures {
  companion object {
    val player = CreatureType(name = "Hero",
        category = CreatureCategory.player
    )
    val sporeColony = CreatureType(
        name = "Spore Colony",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 3
        ),
        abilities = listOf(
            Pair(Abilities.punch, 1)
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
        abilities = listOf(
            Pair(Abilities.lightning, 1)
        ),
        level = 2,
        frequency = 4
    )
    val droid = CreatureType(
        name = "Droid",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Pair(Abilities.shotgun, 1),
            Pair(Abilities.circumvent, 0)
        ),
        level = 2,
        frequency = 4
    )
    val bombCaster = CreatureType(
        name = "Bomb Caster",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Pair(Abilities.grenade, 1)
        ),
        level = 2,
        frequency = 4
    )
    val staticNettle = CreatureType(
        name = "Static Nettle",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.ethereal to 18,
            Element.plant to 15
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 1),
            Pair(Abilities.thorns, 4)
        ),
        level = 3,
        frequency = 3
    )
    val venomousVine = CreatureType(
        name = "Venomous Vine",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 8
        ),
        abilities = listOf(
            Pair(Abilities.poison, 1),
            Pair(Abilities.regeneration, 1)
        ),
        level = 2,
        frequency = 3
    )
    val obsidianDwarf = CreatureType(
        name = "Obsidian Dwarf",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.robot to 10
        ),
        abilities = listOf(
            Pair(Abilities.shotgun, 2),
            Pair(Abilities.small, 3)
        ),
        level = 4,
        frequency = 3
    )
    val cyberAngel = CreatureType(
        name = "Cyber Angel",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.ethereal to 12,
            Element.robot to 12
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 2),
            Pair(Abilities.shotgun, 2)
        ),
        level = 3,
        frequency = 3
    )
    val mossTank = CreatureType(
        name = "Moss Tank",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 12,
            Element.robot to 12
        ),
        abilities = listOf(
            Pair(Abilities.grenade, 2),
            Pair(Abilities.punch, 2),
            Pair(Abilities.armor, 0)
        ),
        level = 4,
        frequency = 3
    )
    val cyberEnt = CreatureType(
        name = "Cyber Ent",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 12,
            Element.robot to 18
        ),
        abilities = listOf(
            Pair(Abilities.missileLauncher, 2)
        ),
        level = 5,
        frequency = 2
    )
    val wallofWind = CreatureType(
        name = "Wall of Wind",
        category = CreatureCategory.ally,
        elements = mapOf(
            Element.ethereal to 2
        ),
        abilities = listOf(
            Pair(Abilities.sacrifice, 0)
        ),
        level = 1
    )
    val wallofWeed = CreatureType(
        name = "Wall of Weed",
        category = CreatureCategory.ally,
        elements = mapOf(
            Element.plant to 6
        ),
        abilities = listOf(
            Pair(Abilities.sacrifice, 0),
            Pair(Abilities.regeneration, 0)
        ),
        level = 1
    )
    val golem = CreatureType(
        name = "Golem",
        category = CreatureCategory.ally,
        elements = mapOf(
            Element.robot to 3
        ),
        abilities = listOf(
            Pair(Abilities.punch, 2),
            Pair(Abilities.sacrifice, 0)
        ),
        level = 1
    )
    val hive = CreatureType(
        name = "Hive",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 20
        ),
        abilities = listOf(
            Pair(Abilities.summonSapper, 0)
        ),
        level = 4,
        frequency = 3
    )
    val assassin = CreatureType(
        name = "Assassin",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 14,
            Element.robot to 11
        ),
        abilities = listOf(
            Pair(Abilities.poison, 2),
            Pair(Abilities.circumvent, 0)
        ),
        level = 4,
        frequency = 1
    )
    val underLord = CreatureType(
        name = "UnderLord",
        category = CreatureCategory.enemy,
        elements = mapOf(
            Element.plant to 20,
            Element.robot to 22,
            Element.ethereal to 22
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 2),
            Pair(Abilities.shotgun, 2),
            Pair(Abilities.poison, 2),
            Pair(Abilities.regeneration, 2),
            Pair(Abilities.multipleAttack, 0)
        ),
        level = 6,
        frequency = 2
    )
  }
}

val creatureLibrary: List<CreatureType> = reflectProperties(Creatures.Companion)

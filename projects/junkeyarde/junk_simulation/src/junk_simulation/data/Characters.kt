package junk_simulation.data

import junk_simulation.CharacterCategory
import junk_simulation.CharacterType
import junk_simulation.Element
import junk_simulation.reflectProperties

class Characters {
  companion object {
    val player = CharacterType(name = "Hero",
        category = CharacterCategory.player
    )
    val sporeColony = CharacterType(
        name = "Spore Colony",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.plant to 3
        ),
        abilities = listOf(
            Pair(Abilities.punch, 1)
        ),
        level = 1,
        frequency = 7
    )
    val ghost = CharacterType(
        name = "Ghost",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.ethereal to 5
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 1)
        ),
        level = 2,
        frequency = 6
    )
    val droid = CharacterType(
        name = "Droid",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Pair(Abilities.shotgun, 1),
            Pair(Abilities.circumvent, 0)
        ),
        level = 2,
        frequency = 6
    )
    val bombCaster = CharacterType(
        name = "Bomb Caster",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.robot to 5
        ),
        abilities = listOf(
            Pair(Abilities.grenade, 1)
        ),
        level = 2,
        frequency = 6
    )
    val staticNettle = CharacterType(
        name = "Static Nettle",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.ethereal to 18,
            Element.plant to 15
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 1),
            Pair(Abilities.thorns, 4)
        ),
        level = 3,
        frequency = 5
    )
    val venomousVine = CharacterType(
        name = "Venomous Vine",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.plant to 8
        ),
        abilities = listOf(
            Pair(Abilities.poison, 1),
            Pair(Abilities.regeneration, 1)
        ),
        level = 2,
        frequency = 5
    )
    val obsidianDwarf = CharacterType(
        name = "Obsidian Dwarf",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.robot to 10
        ),
        abilities = listOf(
            Pair(Abilities.shotgun, 2),
            Pair(Abilities.small, 3)
        ),
        level = 4,
        frequency = 5
    )
    val cyberAngel = CharacterType(
        name = "Cyber Angel",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.ethereal to 12,
            Element.robot to 12
        ),
        abilities = listOf(
            Pair(Abilities.lightning, 2),
            Pair(Abilities.shotgun, 2)
        ),
        level = 3,
        frequency = 5
    )
    val mossTank = CharacterType(
        name = "Moss Tank",
        category = CharacterCategory.monster,
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
        frequency = 5
    )
    val cyberEnt = CharacterType(
        name = "Cyber Ent",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.plant to 12,
            Element.robot to 18
        ),
        abilities = listOf(
            Pair(Abilities.missileLauncher, 2)
        ),
        level = 5,
        frequency = 4
    )
    val wallofWind = CharacterType(
        name = "Wall of Wind",
        category = CharacterCategory.ally,
        elements = mapOf(
            Element.ethereal to 2
        ),
        abilities = listOf(
            Pair(Abilities.sacrifice, 0)
        ),
        level = 1
    )
    val wallofWeed = CharacterType(
        name = "Wall of Weed",
        category = CharacterCategory.ally,
        elements = mapOf(
            Element.plant to 6
        ),
        abilities = listOf(
            Pair(Abilities.sacrifice, 0),
            Pair(Abilities.regeneration, 0)
        ),
        level = 1
    )
    val golem = CharacterType(
        name = "Golem",
        category = CharacterCategory.ally,
        elements = mapOf(
            Element.robot to 3
        ),
        abilities = listOf(
            Pair(Abilities.punch, 2),
            Pair(Abilities.sacrifice, 0)
        ),
        level = 1
    )
    val hive = CharacterType(
        name = "Hive",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.plant to 20
        ),
        abilities = listOf(
            Pair(Abilities.summonSapper, 0)
        ),
        level = 4,
        frequency = 5
    )
    val assassin = CharacterType(
        name = "Assassin",
        category = CharacterCategory.monster,
        elements = mapOf(
            Element.plant to 14,
            Element.robot to 11
        ),
        abilities = listOf(
            Pair(Abilities.poison, 2),
            Pair(Abilities.circumvent, 0)
        ),
        level = 4,
        frequency = 2
    )
    val underLord = CharacterType(
        name = "UnderLord",
        category = CharacterCategory.monster,
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
        frequency = 4
    )
  }
}

val characterLibrary: List<CharacterType> = reflectProperties(Characters.Companion)

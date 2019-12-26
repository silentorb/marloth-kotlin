package simulation.combat

import silentorb.mythic.combat.CombatDeck
import silentorb.mythic.combat.CombatDefinitions
import simulation.main.Deck
import simulation.misc.Definitions

fun toModifierDeck(deck: Deck) =
    CombatDeck(
        accessories = deck.accessories,
        modifiers = deck.buffs
    )

fun toCombatDefinitions(definitions: Definitions)=
    CombatDefinitions(
        accessories = definitions.accessories,
        damageTypes = definitions.damageTypes,
        modifiers = definitions.modifiers
    )

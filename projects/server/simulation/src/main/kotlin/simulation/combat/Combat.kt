package simulation.combat

import silentorb.mythic.combat.general.CombatDeck
import silentorb.mythic.combat.general.CombatDefinitions
import silentorb.mythic.combat.spatial.SpatialCombatDeck
import silentorb.mythic.combat.spatial.SpatialCombatDefinitions
import silentorb.mythic.combat.spatial.SpatialCombatWorld
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions

fun toModifierDeck(deck: Deck) =
    CombatDeck(
        accessories = deck.accessories,
        modifiers = deck.modifiers
    )

fun toCombatDefinitions(definitions: Definitions) =
    CombatDefinitions(
        accessories = definitions.accessories,
        damageTypes = definitions.damageTypes,
        modifiers = definitions.modifiers
    )

fun toSpatialCombatDeck(deck: Deck) =
    SpatialCombatDeck(
        accessories = deck.accessories,
        bodies = deck.bodies,
        characterRigs = deck.characterRigs,
        destructibles = deck.destructibles,
        modifiers = deck.modifiers,
        missiles = deck.missiles
    )

fun toSpatialCombatDefinitions(definitions: Definitions) =
    SpatialCombatDefinitions(
        weapons = definitions.weapons
    )

fun toSpatialCombatWorld(world: World) =
    SpatialCombatWorld(
        deck = toSpatialCombatDeck(world.deck),
        definitions = toSpatialCombatDefinitions(world.definitions),
        bulletState = world.bulletState
    )

fun <Output> usingSpatialCombatWorld(transform: (SpatialCombatWorld) -> Output): (World) -> Output = { world ->
  transform(toSpatialCombatWorld(world))
}

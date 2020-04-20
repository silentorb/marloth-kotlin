package simulation.combat

import simulation.combat.general.CombatDeck
import simulation.combat.general.CombatDefinitions
import simulation.combat.spatial.SpatialCombatDeck
import simulation.combat.spatial.SpatialCombatDefinitions
import simulation.combat.spatial.SpatialCombatWorld
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
        actions = deck.actions,
        bodies = deck.bodies,
        characterRigs = deck.characterRigs,
        collisionShapes = deck.collisionShapes,
        destructibles = deck.destructibles,
        modifiers = deck.modifiers,
        missiles = deck.missiles
    )

fun toSpatialCombatDefinitions(definitions: Definitions) =
    SpatialCombatDefinitions(
        actions = definitions.actions,
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

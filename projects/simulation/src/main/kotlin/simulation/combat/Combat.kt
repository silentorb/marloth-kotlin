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
        accessories = deck.accessories
    )

fun toCombatDefinitions(definitions: Definitions) =
    CombatDefinitions(
        accessories = definitions.accessories,
        damageTypes = definitions.damageTypes
    )

fun toSpatialCombatDeck(deck: Deck) =
    SpatialCombatDeck(
        accessories = deck.accessories,
        actions = deck.actions,
        bodies = deck.bodies,
        characterRigs = deck.characterRigs,
        collisionShapes = deck.collisionObjects,
        destructibles = deck.destructibles,
        missiles = deck.missiles,
        targets = deck.targets
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

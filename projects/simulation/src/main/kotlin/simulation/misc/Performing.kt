package simulation.misc

import silentorb.mythic.performing.PerformanceDeck
import silentorb.mythic.performing.PerformanceDefinitions
import simulation.main.Deck

fun toPerformanceDeck(deck: Deck) =
    PerformanceDeck(
        accessories = deck.accessories,
        performances = deck.performances,
        timersFloat = deck.timersFloat
    )

fun toPerformanceDefinitions(definitions: Definitions) =
    PerformanceDefinitions(
        actions = definitions.actions,
        animations = definitions.animations
    )

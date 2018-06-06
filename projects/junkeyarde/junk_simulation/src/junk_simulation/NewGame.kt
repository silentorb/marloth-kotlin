package junk_simulation

import junk_simulation.data.abilityLibrary

fun newWorld() =
    World(
        turn = 1,
        wave = 1,
        characters = mapOf()
    )

fun newAbilitySelectionState() =
    AbilitySelectionState(
        available = abilityLibrary,
        selected = listOf()
    )

fun newGameState() =
    GameState(
        world = newWorld(),
        mode = GameMode.abilitySelection,
        abilitySelectionState = newAbilitySelectionState()
    )
package junk_simulation

import junk_simulation.data.abilityLibrary

fun newWorld() =
    World(
        turn = 1,
        wave = 1,
        characters = mapOf()
    )

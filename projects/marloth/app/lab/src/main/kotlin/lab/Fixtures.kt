package lab

import marloth.integration.AppState

enum class FixtureId {
  merchant
}

val merchantFixture: (AppState) -> AppState = { appState ->
  val world = appState.worlds.last()
  val deck = world.deck
  val character = deck.characters[1L]!!
  appState.copy(
      worlds = appState.worlds.dropLast(1).plus(world.copy(
          deck = deck.copy(
              characters = deck.characters.plus(Pair(1L, character.copy(
                  interactingWith = deck.interactables.keys.first()
              )))
          )
      ))
  )
}

fun applyFixture(fixture: FixtureId): (AppState) -> AppState =
    when (fixture) {
      FixtureId.merchant -> merchantFixture
    }

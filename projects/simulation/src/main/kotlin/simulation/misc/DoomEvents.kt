package simulation.misc

import silentorb.mythic.happenings.Events
import simulation.characters.ModifyLevelEvent
import simulation.characters.maxCharacterLevel
import simulation.main.World

const val doomEventInterval = 10

fun doomEvents(definitions: Definitions, world: World): Events {
  val doom = world.global.doom
  val interval = (doom % doomEventInterval)
  return if (doom > 0 && interval == 0L) {
    val deck = world.deck
    val monsters = deck.characters
        .filterValues { it.faction == Factions.monsters }
        .mapValues { it.value.definition }
        .filterValues { it.level < maxCharacterLevel }

    if (monsters.none())
      listOf()
    else {
      val monster = world.dice.takeOne(monsters.keys)

      listOf(
          ModifyLevelEvent(
              actor = monster,
              offset = 1
          )
      )
    }
  } else
    listOf()
}

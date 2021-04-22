package silentorb.mythic.characters.rigs

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table

// Used to prevent user input for short periods
const val disableFreedomsBuff = "disableFreedomsBuff"

object Freedom {
  const val none = 0
  const val walking = 1
  const val turning = 2
  const val orbiting = 4
  const val acting = 8

  const val all = -1
}

typealias Freedoms = Int

typealias FreedomTable = Table<Freedoms>

fun hasFreedom(freedoms: Freedoms, freedom: Freedoms): Boolean =
    freedoms and freedom != 0

fun hasFreedom(freedomTable: FreedomTable, actor: Id, freedom: Freedoms): Boolean =
    hasFreedom(freedomTable.getOrDefault(actor, Freedom.none), freedom)

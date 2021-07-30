package simulation.updating

import silentorb.mythic.ent.Table
import silentorb.mythic.ent.mapTable
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import simulation.entities.Depiction

const val replaceDepiction = "replaceDepictionCommand"

fun updateDepictions(events: Events, depictions: Table<Depiction>): Table<Depiction> {
  val depictionEvents = events
      .filterIsInstance<Command>()
      .filter { it.type == replaceDepiction }

  return if (depictionEvents.none())
    depictions
  else
    mapTable(depictions) { key, depiction ->
      val replacement = depictionEvents
          .firstOrNull { it.target == key }
          ?.value as? Depiction

      replacement ?: depiction
    }
}

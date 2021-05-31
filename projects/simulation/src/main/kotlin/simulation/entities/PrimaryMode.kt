package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Commands

data class PrimaryMode(
    val type: String,
    val mode: String
)

const val setPrimaryMode = "setPrimaryMode"

fun updatePrimaryModes(commands: Commands): (Id, PrimaryMode) -> (PrimaryMode) = { id, primaryMode ->
  val mode = commands.firstOrNull { it.type == setPrimaryMode && it.target == id }?.value as? String
      ?: primaryMode.mode

  primaryMode.copy(
      mode = mode,
  )
}

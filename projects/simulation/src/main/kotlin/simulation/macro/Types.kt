package simulation.macro

import silentorb.mythic.happenings.Commands
import simulation.main.Deck

typealias Frames = Long
const val minutes: Long = 60 * 60
const val hours: Long = 60 * minutes

typealias MacroDeckUpdater = (Frames, Deck) -> Deck

const val updateMacroCommand = "updateMacro"

data class MacroUpdate(
    val duration: Frames,
    val after: Commands,
)

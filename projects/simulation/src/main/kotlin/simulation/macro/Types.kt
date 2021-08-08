package simulation.macro

import silentorb.mythic.happenings.Commands
import simulation.main.Deck
import simulation.main.Frames

typealias MacroDeckUpdater = (Frames, Deck) -> Deck

const val updateMacroCommand = "updateMacro"

data class MacroUpdate(
    val duration: Frames,
    val after: Commands,
)

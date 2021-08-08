package simulation.macro

import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import simulation.main.Frames
import simulation.main.World

fun newMacroUpdateCommand(update: MacroUpdate) =
    Command(updateMacroCommand, value = update)

fun newMacroUpdate(duration: Frames, after: Commands = listOf()) =
    newMacroUpdateCommand(MacroUpdate(duration, after))

fun getMacroUpdateCommands(commands: Commands) =
    commands.filter { it.type == updateMacroCommand && it.value is MacroUpdate }

fun gatherMacroAfterCommands(commands: Commands): Commands =
    getMacroUpdateCommands(commands)
        .flatMap { (it.value as? MacroUpdate)?.after ?: listOf() }

//fun applyMacroUpdates(commands: Commands, world: World): World {
//  val macroUpdates = getMacroUpdateCommands(commands)
//  assert(macroUpdates.size < 2)
//
//  val macroUpdate = macroUpdates.firstOrNull()?.value as? MacroUpdate
//
//  return if (macroUpdate != null)
//    updateMacro(macroUpdate.duration, world)
//  else
//    world
//}

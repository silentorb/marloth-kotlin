package marloth.clienting.gui.menus.logic

import marloth.clienting.AppOptions
import marloth.clienting.ClientEvent
import marloth.clienting.ClientState
import marloth.clienting.gui.menus.views.options.updateInputOptions

fun updateAppOptions(clientState: ClientState): (AppOptions, ClientEvent) -> AppOptions = { options, command ->
  options.copy(
      display = updateDisplayOptions(clientState, options.display, command),
      input = updateInputOptions(command, options.input),
  )
}

fun updateAppOptions(clientState: ClientState, options: AppOptions): AppOptions =
    clientState.commands.fold(options, updateAppOptions(clientState))

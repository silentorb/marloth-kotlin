package marloth.clienting.gui.menus.logic

import marloth.clienting.*

fun updateAppOptions(clientState: ClientState): (AppOptions, ClientEvent) -> AppOptions = { options, event ->
  options.copy(
      display = updateDisplayOptions(clientState, options.display, event)
  )
}

fun updateAppOptions(clientState: ClientState, options: AppOptions): AppOptions =
    clientState.events.filterIsInstance<ClientEvent>().fold(options, updateAppOptions(clientState))

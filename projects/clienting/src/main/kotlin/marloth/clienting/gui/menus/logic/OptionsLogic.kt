package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.ViewId
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.platforming.WindowMode

fun updateAppOptions(clientState: ClientState): (AppOptions, ClientEvent) -> AppOptions = { options, event ->
  options.copy(
      display = updateDisplayOptions(clientState, options.display, event)
  )
}

fun updateAppOptions(clientState: ClientState, options: AppOptions): AppOptions =
    clientState.events.filterIsInstance<ClientEvent>().fold(options, updateAppOptions(clientState))

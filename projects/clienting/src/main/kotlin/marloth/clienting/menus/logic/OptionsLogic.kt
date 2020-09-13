package marloth.clienting.menus.logic

import marloth.clienting.AppOptions
import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.platforming.WindowMode

fun updateDisplayOptions(options: DisplayOptions, event: ClientEvent): DisplayOptions =
    when (event.type) {
      ClientEventType.setWindowMode -> options.copy(windowMode = event.data as? WindowMode ?: options.windowMode)
      else -> options
    }

fun updateAppOptions(options: AppOptions, event: ClientEvent): AppOptions =
    options.copy(
        display = updateDisplayOptions(options.display, event)
    )

fun updateAppOptions(events: List<ClientEvent>, options: AppOptions): AppOptions =
    events.fold(options, ::updateAppOptions)

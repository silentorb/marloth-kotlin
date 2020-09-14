package marloth.clienting.gui.menus.logic

import marloth.clienting.AppOptions
import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.GuiState
import marloth.clienting.gui.ViewId
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.platforming.WindowMode

fun updateAppOptions(options: AppOptions, event: ClientEvent): AppOptions =
    options.copy(
        display = updateDisplayOptions(options.display, event)
    )

fun updateAppOptions(events: List<ClientEvent>, options: AppOptions): AppOptions =
    events.fold(options, ::updateAppOptions)

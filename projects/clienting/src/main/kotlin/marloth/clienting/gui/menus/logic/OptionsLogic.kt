package marloth.clienting.gui.menus.logic

import marloth.clienting.AppOptions
import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.ClientState
import marloth.clienting.gui.menus.general.setOptionValue
import marloth.clienting.gui.menus.views.options.updateInputOptions

fun updateAppOptions(clientState: ClientState): (AppOptions, ClientEvent) -> AppOptions = { options, command ->
  if (command.type == ClientEventType.setOption) {
    val target = command.target as? String
    val value = command.value
    if (target != null && value != null)
      setOptionValue(options, target, value) as? AppOptions ?: options
    else
      options
  } else
    options.copy(
        display = updateDisplayOptions(clientState, options.display, command),
        input = updateInputOptions(command, options.input),
    )
}

fun updateAppOptions(clientState: ClientState, options: AppOptions): AppOptions =
    clientState.commands.fold(options, updateAppOptions(clientState))

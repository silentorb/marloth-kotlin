package marloth.clienting.gui.menus.logic

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.GuiState
import marloth.clienting.gui.ViewId
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.lookinglass.toPlatformDisplayConfig
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.platforming.WindowMode
import simulation.updating.simulationDelta

data class DisplayChangeState(
    val options: DisplayOptions,
    val timeout: Double? = null
)

fun displayChangeStateLifecycle(displayOptions: DisplayOptions, view: ViewId?, state: DisplayChangeState?): DisplayChangeState? =
    if (setOf(ViewId.displayOptions, ViewId.displayChangeConfirmation).contains(view))
      state ?: DisplayChangeState(options = displayOptions)
    else
      null

fun updateStagingDisplayOptions(options: DisplayOptions, event: ClientEvent): DisplayOptions =
    when (event.type) {
      ClientEventType.setWindowMode -> options.copy(windowMode = event.data as? WindowMode ?: options.windowMode)
      else -> options
    }

fun updateStagingDisplayOptions(
    options: DisplayOptions,
    events: List<ClientEvent>
): DisplayOptions =
    events.fold(options, ::updateStagingDisplayOptions)

const val displayChangeTimeoutSeconds: Int = 10

fun updateDisplayChangeTimeout(view: ViewId?, value: Double?) =
    if (view == ViewId.displayChangeConfirmation)
      if (value == null)
        displayChangeTimeoutSeconds.toDouble()
      else
        value - simulationDelta
    else
      null

fun updateDisplayChangeState(
    displayOptions: DisplayOptions,
    guiState: GuiState,
    events: List<ClientEvent>
): DisplayChangeState? {
  val state = displayChangeStateLifecycle(displayOptions, guiState.view, guiState.displayChange)
  return state?.copy(
      options = updateStagingDisplayOptions(state.options, events),
      timeout = updateDisplayChangeTimeout(guiState.view, state.timeout)
  )
}

fun updateDisplayOptions(options: DisplayOptions, event: ClientEvent): DisplayOptions =
    when (event.type) {
      ClientEventType.setWindowMode -> options.copy(windowMode = event.data as? WindowMode ?: options.windowMode)
      else -> options
    }

fun syncDisplayOptions(
    display: PlatformDisplay,
    previousOptions: DisplayOptions,
    options: DisplayOptions,
    events: List<ClientEvent>
) {
  val preview = events
      .firstOrNull { it.type == ClientEventType.previewDisplayChanges }
      ?.data as? DisplayOptions

  val destination = preview ?: if (options != previousOptions) options else null

  if (destination != null) {
    display.setOptions(toPlatformDisplayConfig(previousOptions), toPlatformDisplayConfig(destination))
  }
}

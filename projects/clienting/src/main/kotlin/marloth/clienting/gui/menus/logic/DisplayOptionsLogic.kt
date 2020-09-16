package marloth.clienting.gui.menus.logic

import marloth.clienting.*
import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.ent.firstNotNull
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
      ClientEventType.setStagingWindowMode -> options.copy(windowMode = event.data as? WindowMode ?: options.windowMode)
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

fun updateDisplayOptions(clientState: ClientState, options: DisplayOptions, event: ClientEvent): DisplayOptions =
    when (event.type) {
      ClientEventType.saveDisplayChange -> clientState.guiStates.values
          .firstNotNull { it.displayChange?.options } ?: options
      else -> options
    }

fun needsWindowChange(previous: DisplayOptions, next: DisplayOptions?): Boolean {
  return next != null && (
      previous.windowMode != next.windowMode ||
          previous.windowedDimensions != next.windowedDimensions ||
          previous.fullscreenDimensions != next.fullscreenDimensions
      )
}

fun setPlatformDisplayOptions(display: PlatformDisplay, previous: DisplayOptions, next: DisplayOptions) {
  display.setOptions(toPlatformDisplayConfig(previous), toPlatformDisplayConfig(next))
}

fun syncDisplayOptions(
    display: PlatformDisplay,
    previous: ClientState,
    next: ClientState,
    previousOptions: DisplayOptions,
    options: DisplayOptions
) {
  val revert = if (next.events
          .filterIsInstance<ClientEvent>()
          .any { it.type == ClientEventType.revertDisplayChanges })
    previous.guiStates.values.firstNotNull { it.displayChange?.options }
  else
    null

  if (revert != null) {
    val k = 0
    setPlatformDisplayOptions(display, revert, options)
  } else {
    val preview = previous.guiStates
        .entries.firstOrNull { (player, guiState) ->
          guiState.view == ViewId.displayOptions &&
              next.commands.any { it.type == GuiCommandType.menuBack && it.target == player }
        }?.value?.displayChange?.options

    val destination = preview ?: if (needsWindowChange(previousOptions, options))
      options
    else
      null

    if (destination != null) {
      val k = 0
      setPlatformDisplayOptions(display, previousOptions, destination)
    }
  }
}

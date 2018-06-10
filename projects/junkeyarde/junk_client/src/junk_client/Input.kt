package junk_client

import haft.Commands
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import org.joml.Vector2i

fun applyInput(event: Any, state: ClientState): ClientState =
    when (event.javaClass.kotlin) {

      AbilitySelectionEvent::class -> {
        state.copy(
            abilitySelectionState = handleAbilitySelectionEvent(
                event as AbilitySelectionEvent, state.abilitySelectionState!!
            )
        )
      }

      else -> throw Error("Unsupported event type.")
    }

enum class CommandType {
  select
}

fun createDeviceHandlers(input: PlatformInput): List<ScalarInputSource> {
  val gamepad = input.getGamepads().firstOrNull()
  return listOf(
      input.KeyboardInputSource,
      input.MouseInputSource,
      if (gamepad != null)
        { trigger: Int -> input.GamepadInputSource(gamepad.id, trigger) }
      else
        disconnectedScalarInputSource
  )
}

data class UserInput(
    val commands: Commands<CommandType>,
    val mousePosition: Vector2i
)

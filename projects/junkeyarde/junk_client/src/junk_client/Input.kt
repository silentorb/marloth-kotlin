package junk_client

import haft.Commands
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import org.joml.Vector2i

enum class CommandType {
  select,
  submit
}

fun applyInput(event: Any, state: ClientState): Pair<ClientState, CommandType?> =
    when (event.javaClass.kotlin) {

      CommandType::class -> Pair(state, event as CommandType)

      AbilitySelectionEvent::class -> {
        Pair(state.copy(
            abilitySelectionState = handleAbilitySelectionEvent(
                event as AbilitySelectionEvent, state.abilitySelectionState!!
            )
        ), null)
      }

      else -> throw Error("Unsupported event type.")
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

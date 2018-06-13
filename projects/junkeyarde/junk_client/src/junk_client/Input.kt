package junk_client

import haft.Commands
import haft.ScalarInputSource
import haft.disconnectedScalarInputSource
import mythic.platforming.PlatformInput
import org.joml.Vector2i
import junk_simulation.Id

enum class CommandType {
  select,
  submit,
  useAbility
}

data class AbilityUseData(
    val abilityId: Id,
    val creatureId: Id?
)

data class GameCommand(
    val type: CommandType,
    val data: Any? = null
)

fun applyInput(event: Any, state: ClientState): Pair<ClientState, GameCommand?> =
    when (event.javaClass.kotlin) {

      CommandType::class -> Pair(state, GameCommand(event as CommandType))

      ShopSelectionEvent::class -> {
        Pair(state.copy(
            shopState = handleAbilitySelectionEvent(
                event as ShopSelectionEvent, state.shopState!!
            )
        ), null)
      }

      EntitySelectionEvent::class -> {
        val battle = state.battle!!
        val (entityType, newId) = (event as EntitySelectionEvent)
        when (entityType) {
          EntityType.ability -> Pair(state.copy(
              battle = battle.copy(
                  selectedEntity = if (newId == battle.selectedEntity)
                    null
                  else
                    newId
              )
          ), null)

          EntityType.creature -> Pair(state.copy(
              battle = battle.copy(
                  selectedEntity = null
              )
          ), GameCommand(CommandType.useAbility, AbilityUseData(battle.selectedEntity!!, newId)))
        }
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

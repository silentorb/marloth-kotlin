package compuquest_client

import haft.HaftCommands
import compuquest_simulation.*
import mythic.spatial.Vector2

typealias InputResult = Pair<ClientState, GameCommand?>

fun deselect(state: ClientState): ClientState =
    state.copy(
        battle = state.battle?.copy(
            selectedEntity = null
        )
    )

fun onUseGlobalAbility(event: GlobalAbilityEvent, state: ClientState): InputResult {
  return Pair(
      deselect(state),
      GameCommand(CommandType.useAbility, Action(
          type = event.actionType,
          actor = event.actor,
          ability = event.abilityId,
          target = null
      )))
}

fun onAbilitySelection(state: ClientState, newId: Id): InputResult {
  val battle = state.battle!!
  return Pair(state.copy(
      battle = battle.copy(
          selectedEntity = if (newId == battle.selectedEntity)
            null
          else
            newId
      )
  ), null)
}

fun onEntitySelection(event: EntitySelectionEvent, state: ClientState, actor: Id?): InputResult {
  val battle = state.battle!!
  val (entityType, newId) = event

  return when (entityType) {
    EntityType.ability -> onAbilitySelection(state, newId)

    EntityType.creature -> Pair(
        deselect(state),
        GameCommand(CommandType.useAbility, Action(
            type = ActionType.attack,
            actor = actor!!,
            ability = battle.selectedEntity!!,
            target = newId
        )))
  }
}


fun applyInput(event: Any, state: ClientState, actor: Id?): InputResult =
    when (event.javaClass.kotlin) {

      CommandType::class -> Pair(state, GameCommand(event as CommandType))

      ShopSelectionEvent::class -> {
        Pair(state.copy(
            shopState = handleAbilitySelectionEvent(
                event as ShopSelectionEvent, state.shopState!!
            )
        ), null)
      }

      EntitySelectionEvent::class -> onEntitySelection(event as EntitySelectionEvent, state, actor)

      GlobalAbilityEvent::class -> onUseGlobalAbility(event as GlobalAbilityEvent, state)

      else -> throw Error("Unsupported event type.")
    }

data class UserInput(
    val commands: HaftCommands,
    val mousePosition: Vector2
)

package mythic.bloom

import mythic.bloom.next.Flower
import mythic.bloom.next.Seed

typealias IdSelector<T> = (T) -> String

data class SelectionState(
    val selection: Set<String>
)

val selectionState = existingOrNewState {
  SelectionState(
      selection = setOf()
  )
}

val singleSelectionState = existingOrNewState {
  SelectionState(
      selection = setOf()
  )
}

typealias SelectionLogic = (Set<String>, String) -> Set<String>

//val singleSelection: SelectionLogic = { _, item ->
//  setOf(item)
//}

val optionalSingleSelection: SelectionLogic = { selection, item ->
  if (selection.contains(item))
    setOf()
  else
    setOf(item)
}

fun <T> selectable(key: String, selectionLogic: SelectionLogic, idSelector: IdSelector<T>): (T) -> LogicModule =
    { seed ->
      { (bloomState) ->
        val state = selectionState(bloomState.bag[key])
        val id = idSelector(seed)
        val selection = selectionLogic(state.selection, id)
        if (selection.none())
          mapOf(key to setOf<String>())
        else {
          val newState = SelectionState(
              selection = selection
          )

          mapOf(key to newState)
        }
      }
    }

private fun childSelected2(key: String): LogicModuleTransform = logicWrapper { bundle, result ->
  if (bundle.state.bag[key] != null)
    null
  else
    result
}

//fun selectableList(key: String): LogicModule = { seed ->
//  childSelected2(key)(onClickPersisted(key, basicSelectableMenu(seed)))
//}

//fun <T> persistedSelectable(key: String, selectionLogic: SelectionLogic, idSelector: IdSelector<T>): (T) -> LogicModule =
//    { seed ->
//      onClickPersisted(key, selectable(key, selectionLogic, idSelector)(seed))
//    }

fun selectableFlower(key: String, id: String, flower: (Seed, Boolean) -> Flower): Flower = { seed ->
  val state = selectionState(seed.bag[key])
  val selected = state.selection.contains(id)
  flower(seed, selected)(seed)
}

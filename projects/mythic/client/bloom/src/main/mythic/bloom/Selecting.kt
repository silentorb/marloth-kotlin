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
      onClick({ (bloomState) ->
        val state = selectionState(bloomState.bag[key])
        val id = idSelector(seed)
        val selection = selectionLogic(state.selection, id)
        if (selection.none())
          null
        else {
          val newState = SelectionState(
              selection = selection
          )

          mapOf(key to newState)
        }
      })
    }

fun depictSelectable(key: String, id: String, depiction: (Seed, Boolean) -> Depiction): Flower = { seed ->
  val state = selectionState(seed.bag[key])
  val selected = state.selection.contains(id)
  depict(depiction(seed, selected))(seed)
}

fun selectableFlower(key: String, id: String, flower: (Seed, Boolean) -> Flower): Flower = { seed ->
  val state = selectionState(seed.bag[key])
  val selected = state.selection.contains(id)
  flower(seed, selected)(seed)
}

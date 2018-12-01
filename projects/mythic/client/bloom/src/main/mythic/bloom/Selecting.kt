package mythic.bloom

typealias IdSelector<T> = (T) -> String

data class SelectionState(
    val selection: Set<String>
)

val selectionState = getExistingOrNewState {
  SelectionState(
      selection = setOf()
  )
}

typealias SelectionLogic = (Set<String>, String) -> Set<String>

val singleSelection: SelectionLogic = { _, item ->
  setOf(item)
}

val optionalSingleSelection: SelectionLogic = { selection, item ->
  if (selection.contains(item))
    setOf()
  else
    setOf(item)
}

fun selectionInteraction(key: String, selectionLogic: SelectionLogic, itemId: String): LogicModule = { bloomState, bounds ->
  if (isClickInside(bounds, bloomState.input)) {
    val state = selectionState(bloomState.bag[key])
    val newState = SelectionState(
        selection = selectionLogic(state.selection, itemId)
    )
    mapOf(key to newState)
  } else
    mapOf()
}

fun <T> list(arrangement: LengthArrangement, listItem: ListItem<T>): ListFlower<T> = { items ->
  { b ->
    val lengths = items.map { listItem.length }
    val flowers = items.map(listItem.itemFlower)
    applyBounds(b.bag, flowers, arrangement(b.bounds, lengths))
  }
}

typealias ChildInteraction<T> = (PreparedChildren<T>) -> LogicModule

fun <T> selectable(key: String, selectionLogic: SelectionLogic, idSelector: IdSelector<T>): ChildInteraction<T> = { (items, _, itemBounds) ->
  { bloomState, bounds ->
    val state = selectionState(bloomState.bag[key])
    val newState = if (isClickInside(bounds, bloomState.input)) {
      val selectedIndex = itemBounds.indexOfFirst { isInBounds(bloomState.input.current.mousePosition, it) }
      if (selectedIndex != -1) {
        val id = idSelector(items.toList()[selectedIndex])
        SelectionState(
            selection = selectionLogic(state.selection, id)
        )
      } else
        state
    } else
      state

    mapOf(key to newState)
  }
}

fun <T> list(arranger: ChildArranger<T>, interaction: ChildInteraction<T>): ListFlower<T> = { items ->
  { seed ->
    val preparedChildren = arranger(seed.bounds, items)
    val (_, flowers, itemBounds) = preparedChildren
    applyBounds(seed.bag, flowers, itemBounds)
        .plus(
            Box(
                bounds = seed.bounds,
                logic = interaction(preparedChildren)
            )
        )
  }
}

//fun <T> selectable(selectionLogic: SelectionLogic, idSelector: IdSelector<T>): (String, ListItem<T>) -> ListItem<T> =
//    { key, listItem ->
//      listItem.copy(
//          itemFlower = { item ->
//            { seed: Seed ->
//              listItem.itemFlower(item)(seed)
//                  .plus(
//                      Box(
//                          bounds = seed.bounds,
//                          logic = selectionInteraction(key, selectionLogic, idSelector(item))
//                      )
//                  )
//            }
//          }
//      )
//    }

fun depictSelectable(key: String, id: String, depiction: (Seed, Boolean) -> Depiction): Flower = { seed ->
  val state = selectionState(seed.bag[key])
  val selected = state.selection.contains(id)
  depict(depiction(seed, selected))(seed)
}

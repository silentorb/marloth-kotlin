package mythic.bloom

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

val singleSelection: SelectionLogic = { _, item ->
  setOf(item)
}

val optionalSingleSelection: SelectionLogic = { selection, item ->
  if (selection.contains(item))
    setOf()
  else
    setOf(item)
}

typealias ChildInteraction<T> = (PreparedChildren<T>) -> LogicModule

fun <T> selectable(key: String, selectionLogic: SelectionLogic, idSelector: IdSelector<T>): ChildInteraction<T> = { (items, _, itemBounds) ->
  onClick(persist(key) { (bloomState) ->
    val state = selectionState(bloomState.bag[key])
    val selectedIndex = itemBounds.indexOfFirst { isInBounds(bloomState.input.current.mousePosition, it) }
    val newState = if (selectedIndex != -1) {
      val id = idSelector(items.toList()[selectedIndex])
      SelectionState(
          selection = selectionLogic(state.selection, id)
      )
    } else
      state

    mapOf(key to newState)
  })
}

fun <T> list(arranger: ChildArranger<T>, interaction: ChildInteraction<T>): ListFlower<T> = { items ->
  { seed ->
    val preparedChildren = arranger(seed.bounds, items)
    val (_, flowers, itemBounds) = preparedChildren
    val childBoxes = applyBounds(seed.bag, flowers, itemBounds)
    Blossom(
        boxes = childBoxes
            .plus(
                Box(
                    bounds = accumulatedBounds(childBoxes),
                    logic = interaction(preparedChildren)
                )
            ),
        bounds = seed.bounds // TODO: Not sure if seed.bounds is the right bounds.
    )

  }
}

fun depictSelectable(key: String, id: String, depiction: (Seed, Boolean) -> Depiction): Flower = { seed ->
  val state = selectionState(seed.bag[key])
  val selected = state.selection.contains(id)
  depict(depiction(seed, selected))(seed)
}

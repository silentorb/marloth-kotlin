package metaview

import javafx.scene.Node
import javafx.scene.control.ChoiceDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

typealias KeyHandler = (Emitter, State) -> Unit

fun nodeFunctionDialog(title: String): String? {
  val choices = nodeDefinitions.keys
  val dialog = ChoiceDialog(choices.first(), choices)
  dialog.title = title
  dialog.contentText = "Function"
  dialog.headerText = null
  dialog.graphic = null
  val result = dialog.showAndWait()
  return if (!result.isEmpty)
    result.get()
  else null
}

val addNodeDialog: KeyHandler = { emit, _ ->
  val name = nodeFunctionDialog("Add Node")
  if (name!= null) {
    emit(Event(EventType.addNode, name))
  }
}

val insertNodeDialog: KeyHandler = { emit, state ->
  if (state.gui.graphInteraction.portSelection.any()) {
    val name = nodeFunctionDialog("Insert Node")
    if (name != null) {
      emit(Event(EventType.insertNode, name))
    }
  }
}

val keyEvents: Map<KeyCode, EventType> = mapOf(
    KeyCode.C to EventType.connecting
)

val keyHandlers: Map<KeyCode, KeyHandler> = mapOf(
    KeyCode.A to addNodeDialog,
    KeyCode.I to insertNodeDialog
)

fun listenForKeypresses(node: Node, emit: Emitter, state: () -> State) {
  node.addEventHandler(KeyEvent.KEY_PRESSED) { event ->
    val type = keyEvents[event.code]
    if (type != null)
      emit(Event(type))
    else {
      val handler = keyHandlers[event.code]
      if (handler != null)
        handler(emit, state())
    }
  }
}

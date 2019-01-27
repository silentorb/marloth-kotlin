package metahub.metaview.front

import javafx.scene.Node
import javafx.scene.control.ChoiceDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import metahub.metaview.common.Emitter
import metahub.metaview.common.Event
import metahub.metaview.common.CommonEvent

typealias KeyHandler = (Emitter, CommonState) -> Unit

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
  if (name != null) {
    emit(Event(CommonEvent.addNode, name))
  }
}

val insertNodeDialog: KeyHandler = { emit, state ->
  if (state.gui.graphInteraction.portSelection.any()) {
    val name = nodeFunctionDialog("Insert Node")
    if (name != null) {
      emit(Event(CommonEvent.insertNode, name))
    }
  }
}

val duplicateNodeHandler: KeyHandler = { emit, state ->
  val selections = state.gui.graphInteraction
  val node = selections.nodeSelection.firstOrNull() ?: selections.portSelection.firstOrNull()?.node
  if (node != null) {
    emit(Event(CommonEvent.duplicateNode, node))
  }
}

val keyEvents: Map<KeyCode, CommonEvent> = mapOf(
    KeyCode.C to CommonEvent.connecting
)

val keyHandlers: Map<KeyCode, KeyHandler> = mapOf(
    KeyCode.A to addNodeDialog,
    KeyCode.D to duplicateNodeHandler,
    KeyCode.I to insertNodeDialog
)

fun listenForKeypresses(node: Node, emit: Emitter, state: () -> CommonState) {
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

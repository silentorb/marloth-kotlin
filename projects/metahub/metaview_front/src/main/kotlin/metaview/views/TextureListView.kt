package metaview.views

import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import metaview.*

fun textureList(emit: Emitter, state: State): Node {
  val list = ListView<String>()
  list.isEditable = true
  list.cellFactory = TextFieldListCell.forListView()
  state.graphNames.forEach { list.items.add(it) }

  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(EventType.textureSelect, name))
  }

  list.setOnEditCommit { event ->
    val change = Renaming(list.items[event.index], event.newValue)
    list.items[event.index] = event.newValue
    emit(Event(EventType.renameTexture, change))
  }

  list.selectionModel.select(state.gui.activeGraph)
  return list
}
package metaview.views

import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import metaview.Emitter
import metaview.Event
import metaview.EventType

fun textureList(emit: Emitter, textures: List<String>): Node {
  val list = ListView<String>()
  list.isEditable = true
  list.cellFactory = TextFieldListCell.forListView()
  textures.forEach { list.items.add(it) }
  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(EventType.textureSelect, name))
  }

  list.setOnEditCommit { event ->
    val change = Pair(list.items[event.index], event.newValue)
    list.items[event.index] = event.newValue
    emit(Event(EventType.renameTexture, change))
  }
  list.selectionModel.select(0)
  return list
}
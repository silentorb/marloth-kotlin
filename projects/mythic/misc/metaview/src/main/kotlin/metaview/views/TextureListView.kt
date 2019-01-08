package metaview.views

import javafx.scene.Node
import javafx.scene.control.ListView
import metaview.Emitter
import metaview.Event
import metaview.EventType

fun textureList(emit: Emitter, textures: List<String>): Node {
  val list = ListView<String>()

  textures.forEach { list.items.add(it) }
  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(EventType.textureSelect, name))
  }
  list.selectionModel.select(0)
  return list
}
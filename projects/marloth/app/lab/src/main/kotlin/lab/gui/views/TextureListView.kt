package lab.gui.views

import javafx.scene.Node
import javafx.scene.control.ListView
import lab.gui.Emitter
import lab.gui.Event
import lab.gui.EventType
import rendering.texturing.listProceduralTextures

fun textureList(emit: Emitter): Node {
  val list = ListView<String>()

  val textures = listProceduralTextures()
  textures.forEach { list.items.add(it.second) }
  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(EventType.textureSelect, name))
  }
  return list
}
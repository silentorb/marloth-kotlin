package metaview.views

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.VBox
import metaview.Emitter
import metaview.Event
import metaview.EventType
import metaview.State

fun textureList(emit: Emitter, state: State): Node {
//  val panel = VBox()
  val list = ListView<String>()
//  val plus = Button("New")
//  var editingNewItem = false

//  plus.setOnMouseClicked {
//    val index = 0
//    list.items.add(index, "")
//    list.edit(index)
//    editingNewItem = true
//  }

  list.isEditable = true
  list.cellFactory = TextFieldListCell.forListView()
  state.textures.forEach { list.items.add(it) }
  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(EventType.textureSelect, name))
  }

//  list.setOnEditCancel { event ->
//    if (editingNewItem) {
//      editingNewItem = false
//      list.items.removeAt(event.index)
//    }
//  }

  list.setOnEditCommit { event ->
    //    if (editingNewItem) {
//      editingNewItem = false
//      emit(Event(EventType.newTexture, list.items[event.index]))
//    } else {
    val change = Pair(list.items[event.index], event.newValue)
    list.items[event.index] = event.newValue
    emit(Event(EventType.renameTexture, change))
//    }
  }
  list.selectionModel.select(state.gui.activeGraph)
  return list
//  panel.children.addAll(plus, list)
//  return panel
}
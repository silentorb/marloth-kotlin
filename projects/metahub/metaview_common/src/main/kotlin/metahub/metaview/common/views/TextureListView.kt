package metahub.metaview.common.views

import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import metahub.metaview.common.Emitter
import metahub.metaview.common.Event
import metahub.metaview.common.CommonEvent
import metahub.metaview.common.Renaming

fun textureList(emit: Emitter, state: CommonState): Node {
  val list = ListView<String>()
  list.isEditable = true
  list.cellFactory = TextFieldListCell.forListView()
  state.graphNames.forEach { list.items.add(it) }

  list.setOnMouseClicked {
    val name = list.selectionModel.selectedItem.toString()
    emit(Event(CommonEvent.graphSelect, name))
  }

  list.setOnEditCommit { event ->
    val change = Renaming(list.items[event.index], event.newValue)
    list.items[event.index] = event.newValue
    emit(Event(CommonEvent.renameGraph, change))
  }

  list.selectionModel.select(state.gui.activeGraph)
  return list
}
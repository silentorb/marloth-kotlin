package metaview.views

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import metaview.Emitter
import metaview.Event
import metaview.EventType
import metaview.nodeDefinitions

fun newTextureDialog(emit: Emitter) {
  val dialog = TextInputDialog()
  dialog.title = "New Texture"
  dialog.contentText = "Name"
  dialog.headerText = null
  dialog.graphic = null
  val result = dialog.showAndWait()
  if (!result.isEmpty) {
    emit(Event(EventType.newTexture, result.get()))
  }
}

val fileMenu: (Emitter) -> Menu = { emit ->
  val file = Menu("_File")

  val newTexture = MenuItem("_New Texture")
  newTexture.onAction = EventHandler { newTextureDialog(emit) }
  newTexture.accelerator = KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN)

  val refresh = MenuItem("_Refresh")
  refresh.onAction = EventHandler { emit(Event(EventType.refresh)) }
  refresh.accelerator = KeyCodeCombination(KeyCode.F5)

  file.items.addAll(newTexture, refresh)
  file
}

val editMenu: (Emitter) -> Menu = { emit ->
  val edit = Menu("_Edit")
  val deleteNode = MenuItem("_Delete")
  deleteNode.setOnAction { emit(Event(EventType.deleteSelected)) }
  deleteNode.accelerator = KeyCodeCombination(KeyCode.DELETE)

  edit.items.addAll(deleteNode)
  edit
}

val addMenu: (Emitter) -> Menu = { emit ->
  val add = Menu("_Add")
  val options = nodeDefinitions.map { (key, _) ->
    val item = MenuItem(key)
    item.setOnAction { emit(Event(EventType.addNode, key)) }
    item
  }
  add.items.addAll(options)
  add
}

fun menuBarView(emit: Emitter): Node {
  val menu = MenuBar()
  menu.menus.addAll(listOf(
      fileMenu,
      editMenu,
      addMenu
  ).map { it(emit) })
  return menu
}
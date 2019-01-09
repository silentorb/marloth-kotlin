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

fun menuBarView(emit: Emitter): Node {
  val menu = MenuBar()
  val file = Menu("File")

  val newTexture = MenuItem("New Texture")
  newTexture.onAction = EventHandler { newTextureDialog(emit) }
  newTexture.accelerator = KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN)

  val refresh = MenuItem("Refresh")
  refresh.onAction = EventHandler { emit(Event(EventType.refresh)) }
  refresh.accelerator = KeyCodeCombination(KeyCode.F5)

  file.items.addAll(newTexture, refresh)
  menu.menus.addAll(file)
  return menu
}
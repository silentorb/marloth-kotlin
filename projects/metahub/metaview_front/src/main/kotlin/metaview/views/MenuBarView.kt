package metaview.views

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
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

typealias MenuItemDefinition = (Emitter) -> MenuItem
typealias MenuItems = List<MenuItemDefinition>

private fun menuItem(name: String, handler: (Emitter) -> Unit, keyCombination: KeyCombination): MenuItemDefinition = { emit ->
  val item = MenuItem(name)
  item.setOnAction { handler(emit) }
  item.accelerator = keyCombination
  item
}

fun menu(name: String, items: MenuItems): (Emitter) -> Menu = { emit ->
  val result = Menu(name)
  result.items.addAll(items.map { it(emit) })
  result
}

val editMenu: MenuItems = listOf(
    menuItem("_Delete", { it(Event(EventType.deleteSelected)) }, KeyCodeCombination(KeyCode.DELETE)),
    menuItem("_Undo", { it(Event(EventType.undo)) }, KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN)),
    menuItem("_Redo", { it(Event(EventType.redo)) }, KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN))
)

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
      menu("_Edit", editMenu),
      addMenu
  ).map { it(emit) })
  return menu
}
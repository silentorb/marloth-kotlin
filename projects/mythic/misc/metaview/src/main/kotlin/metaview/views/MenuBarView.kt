package metaview.views

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import metaview.Emitter
import metaview.Event
import metaview.EventType

fun menuBarView(emit: Emitter): Node {
  val menu = MenuBar()
  val file = Menu("File")
  val refresh = MenuItem("Refresh")
  refresh.onAction = EventHandler { emit(Event(EventType.refresh)) }
  refresh.setAccelerator(KeyCodeCombination(KeyCode.F5))
  file.items.addAll(refresh)
  menu.menus.addAll(file)
  return menu
}
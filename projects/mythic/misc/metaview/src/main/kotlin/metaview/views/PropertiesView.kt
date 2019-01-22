package metaview.views

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import metahub.InputValue
import metahub.getInputValue
import metaview.*
import mythic.ent.Id

fun selectedNode(state: State): Id? =
    state.gui.graphInteraction.nodeSelection.firstOrNull()
        ?: state.gui.graphInteraction.portSelection.firstOrNull()?.node

fun ifSelectedNode(action: (State, Id) -> Node): (State) -> Node = { state ->
  val id = selectedNode(state)
  if (id == null || id == 0L)
    VBox()
  else
    action(state, id)
}

fun propertiesView(emit: Emitter) = ifSelectedNode { state, id ->
  val panel = VBox()
  panel.spacing = 5.0
  panel.alignment = Pos.BASELINE_CENTER
  val graph = state.graph!!
  val functionName = graph.functions[id]!!
  val label = Label(functionName)

  val grid = GridPane()
  grid.hgap = 10.0
  grid.vgap = 10.0
  grid.padding = Insets(10.0)

  panel.children.addAll(label, grid)

  val definition = nodeDefinitions[functionName]!!

  definition.inputs.entries.forEachIndexed { index, (name, input) ->
    val viewFactory = valueViews[input.type]
    if (viewFactory != null) {
      val propertyLabel = Label(name)
      val value = getInputValue(graph)(id, name)!!
      val changed: OnChange = { newValue, preview ->
        //          if (newValue != value) {
        val data = InputValue(
            node = id,
            port = name,
            value = newValue
        )
        emit(Event(EventType.inputValueChanged, data, preview))
//          }
      }

      val view = viewFactory(input)(value.value, changed)

      GridPane.setConstraints(propertyLabel, 1, index + 1)
      GridPane.setConstraints(view, 2, index + 1)
      grid.children.addAll(propertyLabel, view)
    }
  }
  panel
}
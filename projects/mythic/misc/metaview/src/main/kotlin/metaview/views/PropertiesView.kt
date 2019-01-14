package metaview.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import metahub.Engine
import metahub.InputValue
import metahub.OutputValues
import metahub.getInputValue
import metaview.*

fun propertiesView(emit: Emitter, engine: Engine, state: State, values: OutputValues): Node {
  val id = state.graphInteraction.nodeSelection.firstOrNull() ?: state.graphInteraction.portSelection.firstOrNull()?.node
  return if (id == null) {
    VBox()
  } else {
    val panel = VBox()
    panel.spacing = 5.0
    panel.alignment = Pos.BASELINE_CENTER
    val graph = state.graph!!
    val previewImage = outputImage(getNodePreviewBuffer(graph, id, values[id]!!), 100.0)
    val functionName = graph.functions[id]!!
    val label = Label(functionName)

    panel.children.addAll(previewImage, label)

    val definition = nodeDefinitions[functionName]!!

    for ((name, input) in definition.inputs) {
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

        val view = viewFactory(value.value, changed)
        panel.children.addAll(view, propertyLabel)
      }
    }
    panel
  }
}
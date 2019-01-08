package metaview.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import metahub.Engine
import metahub.OutputValues
import metaview.*
import java.nio.ByteBuffer

fun propertiesView(emit: Emitter, engine: Engine, state: State, values: OutputValues): Node {
  return if (state.nodeSelection.size != 1) {
    VBox()
  } else {
    val panel = VBox()
    panel.alignment = Pos.BASELINE_CENTER
    val id = state.nodeSelection.first()
    val graph = state.graph!!
    val preview = outputImage(values[id]!! as ByteBuffer, 100.0)
    val functionName = graph.functions[id]!!
    val label = Label(functionName)

    panel.children.addAll(preview, label)

    val definition = nodeDefinitions[functionName]!!
    val nodeValues = graph.values[id] ?: mapOf()

    for ((name, input) in definition.inputs) {
      val propertyLabel = Label(name)
      val value = nodeValues[name]
      val view = if (value != null) {
        val changed: OnChange = { newValue, preview ->
          //          if (newValue != value) {
          val data = InputValueChange(
              node = id,
              input = name,
              value = newValue
          )
          emit(Event(EventType.inputValueChanged, data, preview))
//          }
        }
        valueView(changed, value, input.type)
      } else
        Label("---")

      panel.children.addAll(view, propertyLabel)
    }
    panel
  }
}
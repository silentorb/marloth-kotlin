package metaview.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import metahub.Graph
import metaview.Emitter
import metaview.Event
import metaview.EventType
import mythic.ent.Id
import java.nio.ByteBuffer

fun nodeIcon(emit: Emitter, graph: Graph, id: Id, buffer: ByteBuffer, selection: List<Id>): Node {
  val container = VBox()
  val canvas = outputImage(buffer, nodeLength)
  val name = graph.functions[id] ?: "Unknown"
  val label = Label(name)
  container.alignment = Pos.BASELINE_CENTER
  container.children.addAll(canvas, label)

  container.setOnMouseClicked {
    emit(Event(EventType.nodeSelect, id))
  }

  if (selection.contains(id)) {
    val borderStroke = BorderStroke(Color.BLUEVIOLET, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
    container.border = Border(borderStroke)
  }
  return container
}

package metaview.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import metahub.Graph
import metaview.*
import mythic.ent.Id
import mythic.imaging.floatTextureToBytes
import mythic.imaging.grayscaleTextureToBytes
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun getNodePreviewBuffer(graph: Graph, node: Id, value: Any): ByteBuffer {
  val function = graph.functions[node]!!
  val definition = nodeDefinitions[function]!!
  return when (definition.outputType) {
    bitmapType -> floatTextureToBytes(value as FloatBuffer)
    grayscaleType -> grayscaleTextureToBytes(value as FloatBuffer)
    else -> throw Error("Not supported")
  }
}

fun nodeIcon(emit: Emitter, graph: Graph, id: Id, buffer: ByteBuffer, selection: List<Id>): Node {
  val container = VBox()
  val canvas = outputImage(buffer, nodeLength)
  val name = graph.functions[id] ?: "Unknown"
  val label = Label(name)
  container.alignment = Pos.BASELINE_CENTER
  container.children.addAll(canvas, label)

  container.setOnMouseClicked {
    emit(Event(EventType.selectNode, id))
  }

  if (selection.contains(id)) {
    val borderStroke = BorderStroke(Color.BLUEVIOLET, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
    container.border = Border(borderStroke)
  }
  return container
}


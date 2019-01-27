package metahub.metaview.common.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import metahub.core.Graph
import metahub.metaview.common.Emitter
import metahub.metaview.common.Event
import metahub.metaview.common.CommonEvent
import metahub.metaview.common.NodeDefinitionMap
import metahub.metaview.front.views.newImage
import metahub.metaview.front.views.outputImage

import mythic.ent.Id
import mythic.imaging.floatTextureToBytes
import mythic.imaging.grayscaleTextureToBytes
import java.nio.ByteBuffer
import java.nio.FloatBuffer

fun getNodePreviewBuffer(type: String, value: Any): ByteBuffer {
  return when (type) {
    bitmapType -> floatTextureToBytes(value as FloatBuffer)
    grayscaleType -> grayscaleTextureToBytes(value as FloatBuffer)
    else -> throw Error("Not supported")
  }
}

fun getNodePreviewBuffer(nodeDefinitions: NodeDefinitionMap, graph: Graph, node: Id, value: Any): ByteBuffer {
  val function = graph.functions[node]!!
  val definition = nodeDefinitions[function]!!
  return getNodePreviewBuffer(definition.outputType, value)
}

fun nodeIcon(emit: Emitter, graph: Graph, id: Id, buffer: ByteBuffer, selection: List<Id>): Node {
  val container = VBox()
  val canvas = outputImage(newImage(buffer), nodeLength.toDouble())
  val name = graph.functions[id] ?: "Unknown"
  val label = Label(name)
  container.alignment = Pos.BASELINE_CENTER
  container.children.addAll(canvas, label)

  container.setOnMouseClicked {
    emit(Event(CommonEvent.selectNode, id))
  }

  if (selection.contains(id)) {
    val borderStroke = BorderStroke(Color.BLUEVIOLET, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
    container.border = Border(borderStroke)
  }
  return container
}


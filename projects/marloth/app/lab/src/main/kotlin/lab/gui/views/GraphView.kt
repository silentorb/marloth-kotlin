package lab.gui.views

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import lab.gui.Emitter
import lab.gui.State
import metahub.Engine
import metahub.Graph
import metahub.arrangeGraphStages
import metahub.execute
import mythic.ent.Id
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

const val nodeLength: Double = 75.0
const val nodePadding: Double = 40.0

fun nodeIcon(emit: Emitter, graph: Graph, id: Id, buffer: ByteBuffer): Node {
  val container = VBox()
  val byteArray = ByteArray(buffer.capacity())
  buffer.get(byteArray)
  val canvas = Canvas()
  val image = Image(ByteArrayInputStream(byteArray))
//  image.setStyle("-fx-background-color: blue;")
  canvas.width = nodeLength
//  image.setPrefHeight(nodeLength)
  canvas.height = nodeLength
  canvas.graphicsContext2D.drawImage(image, 0.0, 0.0, nodeLength, nodeLength)
  val name = graph.functions[id] ?: "Unknown"
  val label = Label(name)
  container.alignment = Pos.BASELINE_CENTER
  container.children.addAll(canvas, label)
  return container
}

fun graphView(emit: Emitter, engine: Engine, state: State): Node {
  val stack = StackPane()
  val canvas = Canvas()
  val pane = Pane()
  stack.children.addAll(canvas, pane)
  val graph = state.graph
  if (graph != null) {
    val stride = nodeLength + nodePadding
    val stages = arrangeGraphStages(graph)
    val values = execute(engine, graph, stages)
    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        val icon = nodeIcon(emit, graph, nodeId, values[nodeId]!! as ByteBuffer)
        icon.relocate(nodePadding + x * stride, nodePadding + y * stride)
        pane.children.add(icon)
      }
    }
  }
  return stack
}
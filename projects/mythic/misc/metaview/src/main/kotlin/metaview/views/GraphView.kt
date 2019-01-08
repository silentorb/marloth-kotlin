package metaview.views

import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import metahub.Engine
import metahub.OutputValues
import metahub.arrangeGraphStages
import metahub.execute
import metaview.Emitter
import metaview.State
import mythic.ent.Id
import java.nio.ByteBuffer

const val nodeLength: Double = 75.0
const val nodePadding: Double = 40.0

fun graphView(emit: Emitter, engine: Engine, state: State, stages: List<List<Id>>, values: OutputValues): Node {
  val stack = StackPane()
  val canvas = Canvas()
  val pane = Pane()
  stack.children.addAll(canvas, pane)
  val graph = state.graph
  if (graph != null) {
    val stride = nodeLength + nodePadding
    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        val icon = nodeIcon(emit, graph, nodeId, values[nodeId]!! as ByteBuffer, state.nodeSelection)
        icon.relocate(nodePadding + x * stride, nodePadding + y * stride)
        pane.children.add(icon)
      }
    }
  }
  return stack
}
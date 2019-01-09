package metaview.views

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import metahub.Engine
import metahub.Graph
import metahub.OutputValues
import metaview.Emitter
import metaview.NodeDefinition
import metaview.State
import metaview.nodeDefinitions
import mythic.ent.Id
import org.joml.Vector2d

const val nodeLength: Double = 75.0
const val nodePadding: Double = 40.0
const val portPadding: Double = 40.0

fun getDefinition(graph: Graph, node: Id): NodeDefinition {
  val function = graph.functions[node]!!
  return nodeDefinitions[function]!!
}

fun portLabels(graph: Graph, nodeId: Id): List<Node> {
  val definition = getDefinition(graph, nodeId)
  return definition.inputs.map { input ->
    val label = Label(input.key)
    label
  }
}

fun getBoundsRelativeToParent(parent: Node, child: Node): Point2D {
  val position = child.localToScene(0.0, 0.0)
//  var current = child
//  while (current.parent != parent) {
//    current = current.parent
//    val bounds = current.localToScene(0.0, 0.0)
//    position = position.add(bounds)
//  }

  return position.subtract(parent.localToScene(0.0, 0.0))
}

fun graphView(emit: Emitter, engine: Engine, state: State, stages: List<List<Id>>, values: OutputValues): Node {
  val stack = Pane()
  val canvas = Canvas(800.0, 600.0)
  val pane = Pane()
  stack.children.addAll(canvas, pane)
  val graph = state.graph
  if (graph != null) {
    val nodeNodes = mutableMapOf<Id, Pair<Node, List<Node>>>()
    val strideX = nodeLength + nodePadding + portPadding
    val strideY = nodeLength + nodePadding
    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        {}
        val buffer = getNodePreviewBuffer(graph, nodeId, values[nodeId]!!)
        val hbox = HBox()
        val icon = nodeIcon(emit, graph, nodeId, buffer, state.nodeSelection)
        hbox.relocate(nodePadding + x * strideX, nodePadding + y * strideY)
        val portsPanel = VBox()
        portsPanel.spacing = 5.0
        val portLabels = portLabels(graph, nodeId)
        portsPanel.children.addAll(portLabels)
        nodeNodes[nodeId] = Pair(icon, portLabels)
        hbox.children.addAll(portsPanel, icon)
        pane.children.add(hbox)
      }
    }

    val gc = canvas.graphicsContext2D
//    gc.setFill(Color.BLUE);
//    gc.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight())

    graph.connections.forEach { connection ->
      val input = nodeNodes[connection.input]!!
      val output = nodeNodes[connection.output]!!
      val outputDefinition = getDefinition(graph, connection.output)
      val port = output.second[outputDefinition.inputs.keys.indexOf(connection.port)]
      val a = getBoundsRelativeToParent(pane, input.first)
      val b = getBoundsRelativeToParent(pane, port)
      gc.stroke = Color.GREEN
      gc.lineWidth = 2.0
      gc.strokeLine(
          a.x + input.first.boundsInParent.width + 7,
          a.y + input.first.boundsInParent.height / 2,
          b.x - 7,
          b.y + 10.0
      )
    }
  }

  return stack
}
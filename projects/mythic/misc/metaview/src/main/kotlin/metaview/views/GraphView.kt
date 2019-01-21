package metaview.views

import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import metahub.*
import metaview.*
import mythic.ent.Id

const val nodeLength: Double = 75.0
const val nodePadding: Double = 40.0
const val portPadding: Double = 40.0

fun getDefinition(graph: Graph, node: Id): NodeDefinition {
  val function = graph.functions[node]!!
  return nodeDefinitions[function]!!
}

fun portLabel(port: Port, emit: Emitter, selection: List<Port>): Node {
  val label = Label(port.input)
  label.setOnMouseClicked { emit(Event(EventType.selectInput, port)) }
  if (selection.contains(port)) {
    val borderStroke = BorderStroke(Color.BLUEVIOLET, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
    label.border = Border(borderStroke)
  }
  return label
}

fun portLabels(graph: Graph, emit: Emitter, selection: List<Port>, nodeId: Id): List<Node> {
  val definition = getDefinition(graph, nodeId)
  return definition.inputs
//      .filter { input -> graph.connections.any { it.output == nodeId && it.port == input.key } }
      .map { input ->
        val port = Port(node = nodeId, input = input.key)
        portLabel(port, emit, selection)
      }
}

fun getBoundsRelativeToParent(parent: Node, child: Node): Point2D {
  val position = child.localToScene(0.0, 0.0)
  return position.subtract(parent.localToScene(0.0, 0.0))
}

fun drawConnection(gc: GraphicsContext, pane: Node, nodeNode: Node, port: Node) {
  val a = getBoundsRelativeToParent(pane, nodeNode)
  val b = getBoundsRelativeToParent(pane, port)
  gc.stroke = Color.GREEN
  gc.lineWidth = 2.0
  gc.strokeLine(
      a.x + nodeNode.boundsInParent.width + 7,
      a.y + nodeNode.boundsInParent.height / 2,
      b.x - 7,
      b.y + 10.0
  )
}

fun graphView(emit: Emitter, engine: Engine, state: State, values: OutputValues): Node {
  val stack = Pane()
  val canvas = Canvas()
  val pane = Pane()
  stack.children.addAll(canvas, pane)
  val graph = state.graph
  if (graph != null) {
    val nodeNodes = mutableMapOf<Id, Pair<Node, List<Node>>>()
    val strideX = nodeLength + nodePadding + portPadding
    val strideY = nodeLength + nodePadding
    val stages = arrangeGraphStages(graph)
    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        val buffer = getNodePreviewBuffer(graph, nodeId, values[nodeId]!!)
        val hbox = HBox()
        val icon = nodeIcon(emit, graph, nodeId, buffer, state.gui.graphInteraction.nodeSelection)
        hbox.relocate(nodePadding + x * strideX, nodePadding + y * strideY)
        val portsPanel = VBox()
        portsPanel.spacing = 5.0
        val portLabels = portLabels(graph, emit, state.gui.graphInteraction.portSelection, nodeId)
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
      drawConnection(gc, pane, input.first, port)
//      val a = getBoundsRelativeToParent(pane, input.first)
//      val b = getBoundsRelativeToParent(pane, port)
//      gc.stroke = Color.GREEN
//      gc.lineWidth = 2.0
//      gc.strokeLine(
//          a.x + input.first.boundsInParent.width + 7,
//          a.y + input.first.boundsInParent.height / 2,
//          b.x - 7,
//          b.y + 10.0
//      )
    }

    listOf("diffuse")
        .forEachIndexed { index, name ->
          val label = portLabel(Port(0L, name), emit, state.gui.graphInteraction.portSelection)
          label.relocate(nodePadding + (stages.size + 1) * strideX, nodePadding + (index + 1) * strideY)
          pane.children.add(label)
          val output = graph.outputs[name]
          if (output != null) {
            drawConnection(gc, pane, nodeNodes[output]!!.first, label)
          }
        }
  }

  return stack
}
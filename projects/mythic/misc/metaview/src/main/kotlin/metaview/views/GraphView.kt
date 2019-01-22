package metaview.views

import javafx.application.Platform
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
import org.joml.Vector2i

const val nodeLength: Int = 75
const val nodePadding: Int = 40
const val portPadding: Int = 140

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
      a.x + 183,
      a.y + nodeNode.boundsInParent.height / 2,
      b.x - 7,
      b.y + 10.0
  )
}

private const val strideX = nodeLength + nodePadding + portPadding
private const val strideY = nodeLength + nodePadding

fun nodePosition(x: Int, y: Int): Vector2i {

  return Vector2i(
      nodePadding + x * strideX, nodePadding + y * strideY
  )
}

fun graphCanvas(graph: Graph, stages: List<List<Id>>, pane: Pane, nodeNodes: Map<Id, Pair<Node, List<Node>>>, outputs: Map<String, Node>): Node {
  val tempPosition = nodePosition(stages.size + 2, stages.map { it.size }.sortedDescending().first() + 1)
  val canvas = Canvas(tempPosition.x.toDouble(), tempPosition.y.toDouble())

  val gc = canvas.graphicsContext2D
//    gc.setFill(Color.BLUE);
//    gc.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight())

  Platform.runLater {
    graph.connections.forEach { connection ->
      val input = nodeNodes[connection.input]!!
      val output = nodeNodes[connection.output]!!
      val outputDefinition = getDefinition(graph, connection.output)
      val port = output.second[outputDefinition.inputs.keys.indexOf(connection.port)]
      drawConnection(gc, pane, input.first, port)
    }

    outputs.entries.forEachIndexed { index, (name, label) ->
      val output = graph.outputs[name]
      if (output != null) {
        drawConnection(gc, pane, nodeNodes[output]!!.first, label)
      }
    }
  }

  return canvas
}

fun graphView(emit: Emitter, state: State, values: OutputValues): Node {
  val pane = Pane()
  val graph = state.graph
  if (graph != null) {
    val nodeNodes = mutableMapOf<Id, Pair<Node, List<Node>>>()

    val stages = arrangeGraphStages(graph)

    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        val buffer = getNodePreviewBuffer(graph, nodeId, values[nodeId]!!)
        val hbox = HBox()
        val icon = nodeIcon(emit, graph, nodeId, buffer, state.gui.graphInteraction.nodeSelection)
        val position = nodePosition(x, y)
        hbox.relocate(position.x.toDouble(), position.y.toDouble())
        val portsPanel = VBox()
        portsPanel.spacing = 5.0
        portsPanel.prefWidth = 100.0
        val portLabels = portLabels(graph, emit, state.gui.graphInteraction.portSelection, nodeId)
        portsPanel.children.addAll(portLabels)
        nodeNodes[nodeId] = Pair(hbox, portLabels)
        hbox.children.addAll(portsPanel, icon)
        pane.children.add(hbox)
      }
    }

    val outputs = listOf("diffuse")
        .mapIndexed { index, name ->
          val label = portLabel(Port(0L, name), emit, state.gui.graphInteraction.portSelection)
          label.relocate(nodePadding + (stages.size) * strideX.toDouble(), nodePadding + (index + 1) * strideY.toDouble())
          pane.children.add(label)
          Pair(name, label)
        }.associate { it }

    val canvas = graphCanvas(graph, stages, pane, nodeNodes, outputs)
    pane.children.add(0, canvas)
  }

  return pane
}
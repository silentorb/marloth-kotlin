package metaview.views

import javafx.application.Platform
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import metahub.Graph
import metahub.OutputValues
import metahub.Port
import metahub.arrangeGraphStages
import metaview.*
import mythic.ent.Id
import mythic.imaging.textureOutputTypes
import org.joml.Vector2i

const val nodeLength: Int = 75
const val nodePadding: Int = 40
const val portPadding: Int = 110

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

fun getInputs(graph: Graph, node: Id): Map<String, InputDefinition> {
  val definition = getDefinition(graph, node)
  val additionalInputs = if (definition.variableInputs != null) {
    getDynamicPorts(graph, node, definition.variableInputs)
        .plus(Pair(newPortString, InputDefinition(type = definition.variableInputs)))
  } else
    mapOf()
  return definition.inputs
      .filterValues { connectableTypes.contains(it.type) }
      .plus(additionalInputs)
}

fun portLabels(graph: Graph, emit: Emitter, selection: List<Port>, node: Id): List<Node> {
  return getInputs(graph, node)
      .map { input ->
        val port = Port(node = node, input = input.key)
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

private const val strideX = nodeLength + nodePadding + portPadding
private const val strideY = nodeLength + nodePadding

fun nodePosition(x: Int, y: Int): Vector2i {

  return Vector2i(
      nodePadding + x * strideX, nodePadding + y * strideY
  )
}

fun graphCanvas(graph: Graph, stages: List<List<Id>>, pane: Pane, nodeNodes: Map<Id, Pair<Node, List<Node>>>): Node {
  val tempPosition = nodePosition(stages.size + 2, stages.map { it.size }.sortedDescending().first() + 1)
  val canvas = Canvas(tempPosition.x.toDouble(), tempPosition.y.toDouble())

  val gc = canvas.graphicsContext2D
//    gc.setFill(Color.BLUE);
//    gc.fillRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight())

  Platform.runLater {
    graph.connections.forEach { connection ->
      val input = nodeNodes[connection.input]!!
      val output = nodeNodes[connection.output]!!
      val inputs = getInputs(graph, connection.output)
      val port = output.second[inputs.keys.indexOf(connection.port)]
      drawConnection(gc, pane, input.first, port)
    }
  }

  return canvas
}

fun graphView(emit: Emitter, state: State, values: OutputValues): Node {
  val pane = Pane()
  val graph = state.graph
  if (graph != null) {
    val nodeNodes = mutableMapOf<Id, Pair<Node, List<Node>>>()

    val stages = arrangeGraphStages(textureOutputTypes, graph)
        .plusElement(graph.nodes.filter { textureOutputTypes.contains(graph.functions[it]) })

    stages.forEachIndexed { x, stage ->
      stage.forEachIndexed { y, nodeId ->
        val nodeValue = values[nodeId]
        val icon = if (nodeValue != null) {
          val buffer = getNodePreviewBuffer(graph, nodeId, nodeValue)
          nodeIcon(emit, graph, nodeId, buffer, state.gui.graphInteraction.nodeSelection)
        } else {
          Pane()
        }
        val position = nodePosition(x, y)
        val hbox = HBox()
        hbox.relocate(position.x.toDouble(), position.y.toDouble())
        val portsPanel = VBox()
        portsPanel.spacing = 5.0
        portsPanel.prefWidth = 70.0
        val portLabels = portLabels(graph, emit, state.gui.graphInteraction.portSelection, nodeId)
        portsPanel.children.addAll(portLabels)
        nodeNodes[nodeId] = Pair(hbox, portLabels)
        hbox.children.addAll(portsPanel, icon)
        pane.children.add(hbox)
      }
    }

    val canvas = graphCanvas(graph, stages, pane, nodeNodes)
    pane.children.add(0, canvas)
  }

  return pane
}
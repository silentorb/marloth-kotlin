package metaview

import configuration.saveYamlFile
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import metahub.Graph
import mythic.ent.Id
import java.awt.MouseInfo
import java.awt.Point
import java.io.File

const val newPortString = "New"

fun listProjectTextures(path: String): List<String> {
  return File(path).listFiles()
      .filter { it.extension == "json" }
      .map { it.nameWithoutExtension }
      .sorted()
}

fun saveConfig(config: GuiState) {
  saveYamlFile("metaview.yaml", config)
}

fun isOver(point: Point, node: Node): Boolean {
  val position = node.localToScene(0.0, 0.0)
  val bounds = node.boundsInLocal
  return point.x >= position.x
      && point.x < position.x + bounds.width
      && point.y >= position.y
      && point.y < position.y + bounds.height
}

fun getFocus(root: BorderPane): FocusContext {
  val screenMouse = MouseInfo.getPointerInfo().location
  val mouse = Point(screenMouse.x - globalWindow().x.toInt(), screenMouse.y - globalWindow().y.toInt())
  val isOver = { node: Node? ->
    if (node != null) isOver(mouse, node)
    else false
  }
  return when {
    isOver(root.left) -> FocusContext.graphs
    isOver(root.center) -> FocusContext.graph
    else -> FocusContext.none
  }
}

fun getDynamicPorts(graph: Graph, node: Id, type: String): Map<String, InputDefinition> =
    graph.connections.filter { it.output == node && it.port.toIntOrNull() != null }
        .associate {
          Pair(it.port, InputDefinition(type = type))
        }

fun balanceWeights(index: Int, value: Float, locks: List<Boolean>): (List<Float>) -> List<Float> = { weights ->
  val currentVariableTotal = weights.filterIndexed { i, _ -> i != index && !locks[i] }.sum()
  val lockedAmount = weights.filterIndexed { i, _ -> i != index && locks[i] }.sum()
  val newVariableTotal = 1f - value - lockedAmount
  val scalar = newVariableTotal / currentVariableTotal

  val result = weights.mapIndexed { i, weight ->
    if (i == index)
      value
    else if (locks[i])
      weight
    else
      weight * scalar
  }

  if (result.sum() > 1.000001f) {
    val k = 0
  }
  result
}
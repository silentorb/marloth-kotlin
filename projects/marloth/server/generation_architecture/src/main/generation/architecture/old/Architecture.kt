package generation.architecture.old

import generation.architecture.misc.GenerationConfig
import silentorb.mythic.randomly.Dice
import simulation.main.Hand
import simulation.misc.Graph
import simulation.misc.NodeAttribute
import simulation.misc.Realm
import kotlin.math.ceil
import kotlin.math.floor

const val standardTunnelWidth = 4.5f

fun <T> createOverlappingSeries(gapSize: Float, segmentLength: Float, margin: Float = 0f, action: (Int, Float) -> T): List<T> {
  val length = gapSize - margin * 2f
  val stepCount = ceil(length / segmentLength).toInt()
//  println(stepCount)
  val overflow = stepCount * segmentLength - length
  val stepSize = if (stepCount > 1) segmentLength - overflow / (stepCount - 1) else 0f
  val start = margin + segmentLength / 2f
  return (0 until stepCount).map { step ->
    val stepOffset = start + stepSize * step.toFloat()
    action(step, stepOffset)
  }
}

data class FlushSeries(
    val flushItems: List<Float>,
    val fillerItems: List<Float>
)

fun newFlushSeries(gapSize: Float, segmentLength: Float, margin: Float = 0f): FlushSeries {
  val length = gapSize - margin * 2f
  val stepCount = floor(length / segmentLength).toInt()
  val remainingGap = length - stepCount * segmentLength
  val start = margin + segmentLength / 2f + remainingGap / 2f
  val fillerOffset = remainingGap / 4f
  return FlushSeries(
      flushItems = (0 until stepCount).map { step -> start + segmentLength * step.toFloat() },
      fillerItems = listOf(fillerOffset, gapSize - fillerOffset)
  )
}

fun tunnelNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.attributes.contains(NodeAttribute.tunnel) }

fun roomNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.attributes.contains(NodeAttribute.fullFloor) }

fun nodesWithAllAttributes(graph: Graph, attributes: Set<NodeAttribute>) =
    graph.nodes.values
        .filter { node -> node.attributes.containsAll(attributes) }

typealias Architect = (GenerationConfig, Realm, Dice) -> List<Hand>
typealias HandArchitect = (GenerationConfig, Realm, Dice) -> Hand

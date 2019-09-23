package generation.architecture

import generation.misc.*
import randomly.Dice
import simulation.main.Hand
import simulation.misc.Graph
import simulation.misc.NodeAttribute
import simulation.misc.Realm
import kotlin.math.ceil

const val standardTunnelWidth = 4.5f

fun <T> createSeries(gapSize: Float, segmentLength: Float, margin: Float = 0f, action: (Int, Float) -> T): List<T> {
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

fun tunnelNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.attributes.contains(NodeAttribute.tunnel) }

fun roomNodes(graph: Graph) = graph.nodes.values
    .filter { node -> node.attributes.contains(NodeAttribute.room) }

typealias Architect = (GenerationConfig, Realm, Dice) -> List<Hand>
typealias HandArchitect = (GenerationConfig, Realm, Dice) -> Hand

const val standardWallLength = 4f

private val architectureSteps = listOf(
    placeRoomCeilings,
    placeRoomFloors,
    placeRoomWalls,
    placeTunnelCeilings,
    placeTunnelFloors,
    placeTunnelWalls
)

fun placeArchitecture(config: GenerationConfig, realm: Realm, dice: Dice): List<Hand> =
    architectureSteps.flatMap { it(config, realm, dice) }

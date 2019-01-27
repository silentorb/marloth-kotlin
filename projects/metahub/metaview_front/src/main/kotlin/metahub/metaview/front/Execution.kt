package metahub.metaview.front

import metahub.core.*
import metahub.metaview.views.getDefinition
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias ValueMap = Map<String, () -> Any>

val defaultBitmap: (Int) -> FloatBuffer = { length ->
  BufferUtils.createFloatBuffer(length * length * 3)
}

val defaultGrayscale: (Int) -> FloatBuffer = { length ->
  BufferUtils.createFloatBuffer(length * length)
}

fun fillerTypeValues(length: Int): ValueMap = mapOf(
    bitmapType to defaultBitmap,
    grayscaleType to defaultGrayscale
).mapValues { { it.value(length) } }

fun sanitizeGraph(defaultValues: ValueMap): (Graph) -> Graph = { graph ->
  val changes = graph.nodes.flatMap { node ->
    val definition = getDefinition(graph, node)
    definition.inputs.mapNotNull { input ->
      val connection = graph.connections
          .filter { it.output == node && it.port == input.key }
          .firstOrNull()

      if (connection == null && graph.values.none { it.node == node && it.port == input.key }) {
        Pair(node, input)
      } else
        null
    }
  }

  val newValues = changes.map { (node, input) ->
    val getValue = defaultValues[input.value.type]
    if (getValue == null)
      throw Error("Type ${input.key} cannot be null")
    InputValue(
        value = getValue(),
        node = node,
        port = input.key
    )
  }

  graph.copy(
      values = graph.values.plus(newValues)
  )
}

fun executeSanitized(engine: Engine, graph: Graph): OutputValues {
  val defaultValues = fillerTypeValues(textureLength)
  val tempGraph = sanitizeGraph(defaultValues)(graph)
  return execute(engine, tempGraph)
}
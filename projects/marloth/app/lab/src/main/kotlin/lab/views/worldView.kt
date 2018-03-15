package lab.views

import lab.LabCommandType
import simulation.WorldBoundary
import lab.WorldViewConfig
import lab.utility.drawBorder
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.Vector2i
import org.joml.xy
import org.joml.plus
import org.joml.minus
import org.lwjgl.opengl.GL11
import rendering.Renderer
import simulation.AbstractWorld

val worldPadding = 20f // In screen units

typealias BoxMap = Map<Box, Box>

data class Border(
    val color: Vector4,
    val thickness: Float
)

data class LabLayout(
    val boxes: List<Box>
)

fun <A, B, C> overlap(aList: List<A>, bList: List<B>, merger: (A, B) -> C): List<C> {
  val result = ArrayList<C>(aList.size)
  val iter = bList.iterator()
  for (a in aList) {
    result.add(merger(a, iter.next()))
  }
  return result
}

typealias PositionFunction = (Vector2) -> Vector2

fun getPositionFunction(offset: Vector2, boundary: WorldBoundary, scale: Float): PositionFunction {
  return { position: Vector2 -> offset + (Vector2(position.x, -position.y) - boundary.start.xy) * scale }
}

fun drawGeneratedWorld(bounds: Bounds, canvas: Canvas, abstractWorld: AbstractWorld,
                       config: WorldViewConfig, renderer: Renderer) {
  val scale = getScale(bounds, abstractWorld.boundary)
  val offset = bounds.position + worldPadding
  val getPosition: PositionFunction = getPositionFunction(offset, abstractWorld.boundary, scale)
  drawGrid(canvas, bounds, abstractWorld.boundary, scale)
  if (config.showAbstract)
    drawAbstractWorld(bounds, getPosition, canvas, abstractWorld, renderer)
  if (config.showStructure)
    drawStructureWorld(bounds, getPosition, canvas, abstractWorld)

  canvas.drawSquare(
      offset,
      abstractWorld.boundary.dimensions.xy * scale,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )
}

fun createMapLayout(abstractWorld: AbstractWorld, screenDimensions: Vector2,
                    config: WorldViewConfig, renderer: Renderer): LabLayout {
  val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Bounds, c: Canvas ->
    crop(b, c, { drawGeneratedWorld(b, c, abstractWorld, config, renderer) })
    draw(b, c)
  }

  val panels = listOf(
//      Pair(Measurement(Measurements.pixel, 200f), draw),
      Pair(Measurement(Measurements.stretch, 0f), drawWorld)
  )
  val boxes = overlap(createVerticalBounds(panels.map { it.first }, screenDimensions), panels, { a, b ->
    Box(a, b.second)
  })

  return LabLayout(
      boxes
  )
}

fun renderMainLab(layout: LabLayout, canvas: Canvas) {
  globalState.depthEnabled = false
  globalState.blendEnabled = true
  globalState.blendFunction = Pair(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

  for (box in layout.boxes) {
    box.render(box.bounds, canvas)
  }
}

class WorldView(val config: WorldViewConfig, val abstractWorld: AbstractWorld, val renderer: Renderer) : View {

  override fun createLayout(dimensions: Vector2i): LabLayout {
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    return createMapLayout(abstractWorld, dimensions2, config, renderer)
  }

  override fun updateState(layout: LabLayout, input: InputState, delta: Float) {

  }

  override fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure }
  )
}
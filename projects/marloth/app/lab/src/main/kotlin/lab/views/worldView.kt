package lab.views

import lab.LabCommandType
import simulation.WorldBoundary
import lab.WorldViewConfig
import mythic.bloom.drawBorder
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.Vector2i
import org.joml.xy
import org.joml.plus
import org.joml.minus
import rendering.Renderer
import simulation.AbstractWorld

val worldPadding = 20f // In screen units

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
                    config: WorldViewConfig, renderer: Renderer): Layout {
  val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Bounds, c: Canvas ->
    crop(b, c, { drawGeneratedWorld(b, c, abstractWorld, config, renderer) })
    draw(b, c)
  }

  val panels = listOf(
//      Pair(Measurement(Measurements.pixel, 200f), depictBackground),
      Pair(Measurement(Measurements.stretch, 0f), drawWorld)
  )
  val boxes = arrangeMeasuredList(horizontalArrangement, panels, screenDimensions)

  return Layout(
      boxes
  )
}

class WorldView(val config: WorldViewConfig, val abstractWorld: AbstractWorld, val renderer: Renderer) : View {

  override fun createLayout(dimensions: Vector2i): Layout {
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    return createMapLayout(abstractWorld, dimensions2, config, renderer)
  }

  override fun updateState(layout: Layout, input: InputState, delta: Float) {

  }

  override fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure }
  )
}
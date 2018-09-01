package lab.views.world

import lab.LabCommandType
import lab.WorldViewConfig
import lab.views.LabCommandMap
import mythic.bloom.drawBorder
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.times
import org.joml.Vector2i
import org.joml.plus
import rendering.Renderer
import simulation.Realm

val worldBorderPadding = 20f // In screen units

typealias PositionFunction = (Vector2) -> Vector2

fun getPositionFunction(screenOffset: Vector2, worldOffset: Vector2, scale: Float): PositionFunction {
  return { position: Vector2 -> screenOffset + (Vector2(position.x, -position.y) + worldOffset) * scale }
}

fun drawGeneratedWorld(bounds: Bounds, canvas: Canvas, realm: Realm,
                       config: WorldViewConfig, renderer: Renderer) {
  val padding = realm.boundary.padding
  val innerScale = getScale(bounds, realm.boundary.dimensions + Vector3(padding * 2))
  val outerScale = getScale(bounds, realm.boundary.dimensions)
  val offset = bounds.position + worldBorderPadding
  val getPosition: PositionFunction = getPositionFunction(
      offset,
      Vector2(padding) - realm.boundary.start.xy(),
      innerScale
  )

  drawGrid(canvas, bounds, realm.boundary, innerScale)

  if (config.showAbstract)
    drawAbstractWorld(bounds, getPosition, canvas, realm, renderer)

  if (config.showStructure)
    drawStructureWorld(bounds, getPosition, canvas, realm)

  canvas.drawSquare(
      offset,
      realm.boundary.dimensions.xy() * outerScale,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )
}

fun createMapLayout(realm: Realm, screenDimensions: Vector2,
                    config: WorldViewConfig, renderer: Renderer): List<Box> {
  val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Bounds, c: Canvas ->
    crop(b, c, { drawGeneratedWorld(b, c, realm, config, renderer) })
    draw(b, c)
  }

  val panels = listOf(
//      Pair(Measurement(Measurements.pixel, 200f), depictBackground),
      Pair(Measurement(Measurements.stretch, 0f), drawWorld)
  )
  val boxes = arrangeMeasuredList(measuredHorizontalArrangement, panels, screenDimensions)

  return boxes
}

class WorldView(val config: WorldViewConfig, val realm: Realm, val renderer: Renderer) {

  fun createLayout(dimensions: Vector2i): List<Box> {
    val dimensions2 = Vector2(dimensions.x.toFloat(), dimensions.y.toFloat())
    return createMapLayout(realm, dimensions2, config, renderer)
  }

  fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure }
  )
}
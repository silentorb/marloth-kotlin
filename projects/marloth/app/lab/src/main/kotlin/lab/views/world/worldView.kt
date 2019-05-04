package lab.views.world

import lab.LabCommandType
import lab.WorldViewConfig
import lab.views.LabCommandMap
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.*
import org.joml.Vector2i
import org.joml.plus
import rendering.Renderer
import simulation.Realm

val worldBorderPadding = 20 // In screen units

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
      offset.toVector2(),
      Vector2(padding) - realm.boundary.start.xy(),
      innerScale
  )

  drawGrid(canvas, bounds, realm.boundary, innerScale)

  if (config.showAbstract)
    drawAbstractWorld(bounds, getPosition, canvas, realm, renderer)

  if (config.showStructure)
    drawStructureWorld(bounds, getPosition, canvas, realm)

  canvas.drawSquare(
      offset.toVector2(),
      realm.boundary.dimensions.xy() * outerScale,
      canvas.outline(Vector4(0.6f, 0.5f, 0.5f, 0.5f), 3f)
  )
}

fun createMapLayout(realm: Realm, screenDimensions: Vector2i,
                    config: WorldViewConfig, renderer: Renderer): FlowerOld {
  val draw = { b: Bounds, c: Canvas -> drawBorder(b, c, Vector4(0f, 0f, 1f, 1f)) }
  val drawWorld = { b: Bounds, c: Canvas ->
    crop(b, c, { drawGeneratedWorld(b, c, realm, config, renderer) })
    draw(b, c)
  }

  return depictOld(drawWorld)
//  val panels = listOf(
////      Pair(Measurement(Measurements.pixel, 200f), depictBackground),
//      Pair(Measurement(Measurements.stretch, 0), drawWorld)
//  )
//  val boxes = arrangeMeasuredList(measuredHorizontalArrangement, panels, screenDimensions)
//
//  return boxes
}

class WorldView(val config: WorldViewConfig, val realm: Realm?, val renderer: Renderer) {

  fun createLayout(dimensions: Vector2i): FlowerOld {
    return if (realm != null)
      createMapLayout(realm, dimensions, config, renderer)
    else
      emptyFlowerOld
  }

  fun getCommands(): LabCommandMap = mapOf(
      LabCommandType.toggleAbstractView to { _ -> config.showAbstract = !config.showAbstract },
      LabCommandType.toggleStructureView to { _ -> config.showStructure = !config.showStructure }
  )
}

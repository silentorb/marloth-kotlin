package marloth.clienting.gui

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i
import simulation.World

data class ButtonState(
    val text: String,
    val hasFocus: Boolean
)

fun depictBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

val menuBackground: Depiction = depictBackground(grayTone(0.5f))

fun renderGui(textResources: TextResources, bounds: Bounds, canvas: Canvas, world: World?, menuState: MenuState) {
  if (world != null) {
    val hudBoxes = hudLayout(world)(Seed(bounds = bounds))
    renderLayout(hudBoxes, canvas)
  }

  if (menuState.isVisible) {
    val menu = mainMenu(world != null)
    renderLayout(menuLayout(textResources, menu, bounds, menuState), canvas)
  }
}

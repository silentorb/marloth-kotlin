package marloth.clienting.gui

import haft.HaftCommands
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.CommandType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.platforming.WindowInfo
import mythic.spatial.Vector4
import org.joml.Vector4i
import simulation.World

const val currentViewKey = "currentViewKey"
val currentView = existingOrNewState(currentViewKey) { ViewId.none }

const val menuCommandsKey = "menuCommands"
val menuCommands = existingOrNewState(menuCommandsKey) { listOf<CommandType>() }

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

fun haftToBloom(commands: HaftCommands<CommandType>): List<BloomEvent> =
    commands.mapNotNull {
      when (it.type) {
        CommandType.lookUp -> BloomEvent.up
        CommandType.lookDown -> BloomEvent.down
        CommandType.lookLeft -> BloomEvent.left
        CommandType.lookRight -> BloomEvent.right
        CommandType.moveUp -> BloomEvent.up
        CommandType.moveDown -> BloomEvent.down
        CommandType.moveLeft -> BloomEvent.left
        CommandType.moveRight -> BloomEvent.right
        CommandType.menuBack -> BloomEvent.back
        CommandType.menuSelect -> BloomEvent.activate
        else -> null
      }
    }

fun guiLayout(client: Client, clientState: ClientState, world: World?): Flower {
  val bloomState = clientState.bloomState
  val view = currentView(bloomState.bag)
  return listOfNotNull(
      if (world != null) hudLayout(world) else null,
      if (view == ViewId.mainMenu) {
        val menu = mainMenu(world != null)
        menuFlower(client.textResources, menu)
      } else
        null
  )
      .reduce { a, b -> a + b }
}

//fun renderGui(client: Client, bounds: Bounds, canvas: Canvas, world: World?, clientState: ClientState): BloomState {
//  if (world != null) {
//    val hudBoxes = hudLayout(world)(Seed(bounds = bounds))
//    renderLayout(hudBoxes, canvas)
//  }
//
//  val bloomState = clientState.bloomState
//
//  val view = currentView(bloomState.bag)
//
//  return if (view == ViewId.mainMenu) {
//    val menu = mainMenu(world != null)
//    val layout = menuFlower(client.textResources, menu)
//    val seed = Seed(
//        bag = bloomState.bag,
//        bounds = bounds
//    )
//    val boxes = layout(seed)
//    renderLayout(boxes, canvas)
//    val bloomInputState = newBloomInputState(client.platform.input)
//        .copy(events = listOf())
//    updateBloomState(boxes, bloomState, bloomInputState)
//  } else
//    bloomState
//}

fun plantGui(client: Client, clientState: ClientState, world: World?, windowInfo: WindowInfo): Boxes {
  val bounds = Bounds(Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y))
  val layout = guiLayout(client, clientState, world)
  val seed = Seed(
      bag = clientState.bloomState.bag,
      bounds = bounds
  )
  return layout(seed)
}

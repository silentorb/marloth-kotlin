package marloth.clienting

import commanding.*
import haft.*
import marloth.clienting.gui.MenuActionType
import marloth.clienting.gui.MenuState
import mythic.platforming.Platform
import rendering.Renderer
import scenery.GameScene
import scenery.Screen

val maxPlayerCount = 4

data class ClientInputResult(
    val commands: Commands<CommandType>,
    val state: InputState
)
//{
//
//  operator fun plus(second: ClientInputResult) =
//      ClientInputResult(
//          commands.plus(second.commands),
//          state.copy(inputState = state.inputState.plus(second.state.inputState))
//      )
//}

data class InputProperties(
    val deviceHandlers: List<ScalarInputSource>,
    val waitingDevices: List<GamepadDeviceId>,
    val previousState: ProfileStates<CommandType>,
    val players: List<Int>
)

data class ClientState(
    val input: InputState,
    val menu: MenuState
)

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  //  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
//      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
//      CommandType.menu to { command -> platform.process.close() }
//  )

  val input = ClientInput(platform.input)

  fun getWindowInfo() = platform.display.getInfo()

  fun handleMenuAction(menuAction: MenuActionType) {
    when (menuAction) {
      MenuActionType.quit -> platform.process.close()
    }
  }

  fun update(scenes: List<GameScene>, previousState: ProfileStates<CommandType>):
      Pair<Commands<CommandType>, ProfileStates<CommandType>> {
    throw Error("Outdated.  Will need updating.")
//    val windowInfo = getWindowInfo()
//    renderer.renderGameScenes(scenes, windowInfo)
//    return updateInput(previousState, scenes.map { it.player })
  }

}

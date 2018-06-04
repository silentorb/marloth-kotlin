package junk_client

import haft.CommandHandler
import haft.Commands
import haft.ProfileStates
import junk_common.CommandType
import junk_common.JunkScene
import mythic.platforming.Platform

class Client(val platform: Platform) {
//  val renderer: Renderer = Renderer()
//  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  val gamepadAssignments: MutableMap<Int, Int> = mutableMapOf()
  val keyStrokeCommands: Map<CommandType, CommandHandler<CommandType>> = mapOf(
//      CommandType.switchView to { command -> switchCameraMode(command.target, screens) },
//      CommandType.menu to { command -> platform.process.close() }
  )
//  val playerInputProfiles = defaultGameInputProfiles()
//  val menuInputProfiles = defaultMenuInputProfiles()
  fun getWindowInfo() = platform.display.getInfo()

//  fun handleMenuAction(menuAction: MenuActionType) {
//    when (menuAction) {
//      MenuActionType.quit -> platform.process.close()
//    }
//  }

//  fun checkForNewGamepads(properties: InputProperties): ClientInputResult {
//    val (deviceHandlers, waitingDevices, previousState, players) = properties
//    val profiles = createWaitingGamepadProfiles(waitingDevices.size, gamepadAssignments.size)
//    val result = gatherProfileCommands(profiles, previousState, deviceHandlers)
//    val (commands, nextState) = result
//    var playerCount = players.size
//    val keystrokes = filterKeystrokeCommands(commands, listOf(CommandType.activateDevice, CommandType.joinGame))
//    for (command in keystrokes) {
//      gamepadAssignments[command.target - 10] = playerCount++
//    }
//    return ClientInputResult(keystrokes, nextState)
//  }

//  fun updateGameInput(properties: InputProperties, playerInputProfiles: List<PlayerInputProfile>): ClientInputResult {
//    val profiles = selectActiveInputProfiles(playerInputProfiles, properties.players)
//    val (commands, nextState) = gatherProfileCommands(profiles, properties.previousState, properties.deviceHandlers)
//    handleKeystrokeCommands(commands, keyStrokeCommands)
//    return ClientInputResult(commands, nextState)
//  }

//  fun prepareInput(previousState: ProfileStates<CommandType>, players: List<Int>): InputProperties {
//    platform.input.update()
//    val gamepads = platform.input.getGamepads().map { it.id }
//    val waitingDevices = getWaitingDevices(gamepadAssignments, gamepads)
//    val deviceHandlers = createDeviceHandlers(platform.input, gamepadAssignments)
//        .plus(waitingDevices.map {
//          { trigger: Int -> platform.input.GamepadInputSource(it, trigger) }
//        })
//    return InputProperties(
//        deviceHandlers,
//        waitingDevices,
//        previousState,
//        players
//    )
//  }

  fun update(scene: JunkScene, previousState: ProfileStates<CommandType>):
      Pair<Commands<CommandType>, ProfileStates<CommandType>> {
    throw Error("Outdated.  Will need updating.")
//    val windowInfo = getWindowInfo()
//    renderer.renderGameScenes(scenes, windowInfo)
//    return updateInput(previousState, scenes.map { it.player })
  }

}

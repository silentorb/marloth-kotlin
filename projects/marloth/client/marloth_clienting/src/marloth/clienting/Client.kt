package marloth.clienting

import marloth.clienting.gui.MenuState
import marloth.clienting.gui.initialMenuState
import marloth.clienting.gui.updateMenu
import mythic.platforming.PlatformDisplayConfig
import mythic.platforming.Platform
import rendering.DisplayConfig
import rendering.Renderer
import scenery.Screen

val maxPlayerCount = 4

data class ClientState(
    val input: InputState,
    val menu: MenuState
)

fun newClientState(config: GameInputConfig) =
    ClientState(
        input = InputState(
            device = newInputDeviceState(),
            config = config,
            gameProfiles = defaultGameInputProfiles(),
            menuProfiles = defaultMenuInputProfiles()
        ),
        menu = initialMenuState()
    )

class Client(val platform: Platform, displayConfig: DisplayConfig) {
  val renderer: Renderer = Renderer(displayConfig)
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  fun getWindowInfo() = platform.display.getInfo()
}

fun updateMousePointerVisibility(platform: Platform) {
  val windowHasFocus = platform.display.hasFocus()
  platform.input.isMouseVisible(!windowHasFocus)
}

fun applyClientCommands(client: Client, commands: UserCommands) {
  if (commands.any { it.type == CommandType.quit }) {
    client.platform.process.close()
  }
}

fun updateClient(client: Client, players: List<Int>, previousState: ClientState): Pair<ClientState, UserCommands> {
  updateMousePointerVisibility(client.platform)
  val inputState = previousState.input
  val profiles = selectProfiles(previousState)
  val newDeviceState = updateInputDeviceState(client.platform.input, players, previousState.input, profiles)
  val newCommandState = getCommandState(newDeviceState, inputState.config, players.size)
  val (nextMenuState, menuGlobalCommands) = updateMenu(previousState.menu, newCommandState.commands)

  val allCommands = newCommandState.commands.plus(menuGlobalCommands)

  applyClientCommands(client, allCommands)

  val newClientState = previousState.copy(
      input = previousState.input.copy(
          device = newDeviceState
      ),
      menu = nextMenuState
  )
  return Pair(newClientState, allCommands)
}

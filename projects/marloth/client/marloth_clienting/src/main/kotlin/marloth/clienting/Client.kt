package marloth.clienting

import com.fasterxml.jackson.core.type.TypeReference
import configuration.loadYamlResource
import marloth.clienting.gui.MenuState
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.newMenuState
import marloth.clienting.gui.updateMenu
import mythic.bloom.BloomState
import mythic.bloom.ButtonState
import mythic.drawing.setGlobalFonts
import mythic.platforming.Platform
import org.joml.Vector2i
import rendering.DisplayConfig
import rendering.Renderer
import scenery.Screen
import simulation.World

val maxPlayerCount = 4

data class ClientState(
    val input: InputState,
    val menu: MenuState,
    val bloomState: BloomState
)

fun newClientState(config: GameInputConfig) =
    ClientState(
        input = InputState(
            device = newInputDeviceState(),
            config = config,
            gameProfiles = defaultGameInputProfiles(),
            menuProfiles = defaultMenuInputProfiles()
        ),
        menu = newMenuState(),
        bloomState = BloomState(
            bag = mapOf(),
            input = mythic.bloom.InputState(
                mousePosition = Vector2i(),
                mouseButtons = listOf(ButtonState.up)
            )
        )
    )

fun loadTextResource(): TextResources {
  val typeref = object : TypeReference<TextResources>() {}
  return loadYamlResource("text/english.yaml", typeref)
}

class Client(val platform: Platform, displayConfig: DisplayConfig) {
  val renderer: Renderer = Renderer(displayConfig)
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  val textResources: TextResources = loadTextResource()
  fun getWindowInfo() = platform.display.getInfo()

  init {
    setGlobalFonts(renderer.fonts)
  }
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

fun updateClient(client: Client, players: List<Int>, previousState: ClientState, world: World?): Pair<ClientState, UserCommands> {
  updateMousePointerVisibility(client.platform)
  val inputState = previousState.input
  val profiles = selectProfiles(previousState)
  val newDeviceState = updateInputDeviceState(client.platform.input, players, previousState.input, profiles)
  val newCommandState = getCommandState(newDeviceState, inputState.config, players.size)
  val (nextMenuState, menuGlobalCommands) = updateMenu(previousState.menu, newCommandState.commands, world != null)

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

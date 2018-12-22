package marloth.clienting

import com.fasterxml.jackson.core.type.TypeReference
import configuration.loadYamlResource
import haft.simpleCommand
import marloth.clienting.gui.*
import mythic.bloom.BloomState
import mythic.bloom.Boxes
import mythic.bloom.ButtonState
import mythic.bloom.updateBloomState
import mythic.drawing.setGlobalFonts
import mythic.ent.pipe
import mythic.platforming.Platform
import org.joml.Vector2i
import rendering.DisplayConfig
import rendering.Renderer
import scenery.Screen
import simulation.World

val maxPlayerCount = 4

data class ClientState(
    val input: InputState,
    val bloomState: BloomState
)

fun isGuiActive(state: ClientState): Boolean = currentView(state.bloomState.bag) == ViewId.none

fun newClientState(config: GameInputConfig) =
    ClientState(
        input = InputState(
            device = newInputDeviceState(),
            config = config,
            gameProfiles = defaultGameInputProfiles(),
            menuProfiles = defaultMenuInputProfiles()
        ),
        bloomState = BloomState(
            bag = mapOf(),
            input = mythic.bloom.InputState(
                mousePosition = Vector2i(),
                mouseButtons = listOf(ButtonState.up),
                events = listOf()
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

fun applyClientCommands(client: Client, state: ClientState, commands: UserCommands): ClientState {
  val c = commands.map { it.type }
  if (c.contains(CommandType.quit)) {
    client.platform.process.close()
  }

  return if (c.contains(CommandType.menu) && !isGuiActive(state)) {
    state.copy(
        bloomState = state.bloomState.copy(
            bag = state.bloomState.bag.plus(currentViewKey to ViewId.mainMenu)
        )
    )
  } else
    state
}

fun updateClient(client: Client, players: List<Int>, previousState: ClientState, world: World?, boxes: Boxes): Pair<ClientState, UserCommands> {
  updateMousePointerVisibility(client.platform)
  val inputState = previousState.input
  val profiles = selectProfiles(previousState)
  val newDeviceState = updateInputDeviceState(client.platform.input, players, previousState.input, profiles)
  val newCommandState = getCommandState(newDeviceState, inputState.config, players.size)
  val bloomInputState = newBloomInputState(client.platform.input)
      .copy(events = haftToBloom(newCommandState.commands))
  val bloomState = updateBloomState(boxes, previousState.bloomState, bloomInputState)

  val allCommands = newCommandState.commands
      .plus(menuCommands(bloomState.bag).map { simpleCommand(it, players.first()) })

  val newClientState = pipe(previousState, listOf(
      { state -> applyClientCommands(client, state, allCommands) },
      { state ->
        state.copy(
            input = previousState.input.copy(
                device = newDeviceState
            ))
      }
  ))
  return Pair(newClientState, allCommands)
}

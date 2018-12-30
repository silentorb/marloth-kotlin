package marloth.clienting

import com.fasterxml.jackson.core.type.TypeReference
import configuration.loadYamlResource
import haft.BindingSource
import haft.mapEventsToCommands
import haft.simpleCommand
import marloth.clienting.gui.*
import mythic.aura.AudioState
import mythic.aura.SoundLibrary
import mythic.aura.newAudioState
import mythic.bloom.*
import mythic.drawing.setGlobalFonts
import mythic.ent.pipe
import mythic.platforming.Platform
import rendering.DisplayConfig
import rendering.Renderer
import scenery.Screen
import simulation.World

val maxPlayerCount = 4

data class ClientState(
    val input: InputState,
    val bloomState: BloomState,
    val view: ViewId,
    val audio: AudioState
)

fun isGuiActive(state: ClientState): Boolean = state.view != ViewId.none

fun newClientState(platform: Platform, config: GameInputConfig) =
    ClientState(
        input = newInputState(config),
        bloomState = newBloomState(),
        audio = newAudioState(platform.audio),
        view = ViewId.none
    )

fun loadTextResource(): TextResources {
  val typeref = object : TypeReference<TextResources>() {}
  return loadYamlResource("text/english.yaml", typeref)
}

class Client(val platform: Platform, displayConfig: DisplayConfig) {
  val renderer: Renderer = Renderer(displayConfig)
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  val textResources: TextResources = loadTextResource()
  val soundLibrary: SoundLibrary = mapOf()
  fun getWindowInfo() = platform.display.getInfo()

  init {
    setGlobalFonts(renderer.fonts)
    platform.audio.start()
  }

  fun shutdown(){
    platform.audio.stop()
  }
}

fun updateMousePointerVisibility(platform: Platform) {
  val windowHasFocus = platform.display.hasFocus()
  platform.input.isMouseVisible(!windowHasFocus)
}

fun applyClientCommands(client: Client, commands: UserCommands): (ClientState) -> ClientState = { state ->
  val c = commands.map { it.type }
  if (c.contains(CommandType.quit)) {
    client.platform.process.close()
  }

  if (c.contains(CommandType.menu)) {
    val view = currentView(state.bloomState.bag)
    val newView = if (view == ViewId.mainMenu)
      ViewId.none
    else
      ViewId.mainMenu

    state.copy(
        view = newView
    )
  } else
    state
}

fun getBinding(inputState: InputState, bindingMode: BindingContext): BindingSource<CommandType> = { event ->
  val playerDevice = inputState.deviceMap[event.device]
  if (playerDevice != null) {
    val playerProfile = inputState.playerProfiles[playerDevice.player]!!
    val profile = inputState.profiles[playerProfile]!!
    val binding = profile.bindings[bindingMode]!!.firstOrNull { it.device == playerDevice.device && it.trigger == event.index }
    if (binding != null)
      Pair(binding, playerProfile.toInt())
    else
      null
  } else
    null
}

fun updateClient(client: Client, players: List<Int>, clientState: ClientState, world: World?, boxes: Boxes, delta: Float): Pair<ClientState, UserCommands> {
  updateMousePointerVisibility(client.platform)
  val bindingContext = bindingContext(clientState)
  val getBinding = getBinding(clientState.input, bindingContext)
  val newDeviceStates = updateInputState(client.platform.input, clientState.input)
  val strokes = clientCommandStrokes[bindingContext]!!
  val commands = mapEventsToCommands(newDeviceStates, strokes, getBinding)

  val bloomInputState = newBloomInputState(newDeviceStates.last())
      .copy(events = haftToBloom(commands))
  val bloomState = updateBloomState(boxes, clientState.bloomState, bloomInputState)

  val allCommands = commands
      .plus(menuCommands(bloomState.bag).map { simpleCommand(it, players.first()) })

  val newClientState = pipe(clientState, listOf(
      { state ->
        state.copy(
            input = state.input.copy(
                deviceStates = newDeviceStates
            ),
            bloomState = bloomState,
            view = existingOrNewState(currentViewKey) { state.view }(bloomState.bag)
        )
      },
      // This needs to happen after applying updateBloomState to override flower state settings
      applyClientCommands(client, allCommands),
      updateClientStateAudio(client, delta)
  ))
  return Pair(newClientState, allCommands)
}

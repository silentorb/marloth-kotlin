package marloth.clienting

import haft.BindingSource
import haft.HaftCommand
import haft.mapEventsToCommands
import haft.simpleCommand
import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.loadSounds
import marloth.clienting.gui.*
import marloth.clienting.input.*
import marloth.clienting.input.InputState
import marloth.clienting.textResources.englishTextResources
import mythic.aura.AudioState
import mythic.aura.SoundLibrary
import mythic.aura.newAudioState
import mythic.bloom.*
import mythic.bloom.next.Box
import mythic.bloom.next.LogicModule
import mythic.bloom.next.newBloomState

import mythic.drawing.setGlobalFonts
import mythic.ent.pipe
import mythic.ent.pipe2
import mythic.platforming.Platform
import mythic.spatial.Vector3
import mythic.typography.enumerateTextStyles
import mythic.typography.extractFontSets
import mythic.typography.loadFontSets
import newBloomInputState
import rendering.DisplayConfig
import rendering.Renderer
import rendering.shading.LightingConfig
import scenery.Screen
import scenery.enums.Text
import simulation.main.Deck
import updateInputDeviceStates

const val maxPlayerCount = 4

data class ClientState(
    val input: InputState,
    val bloomState: BloomState,
    val view: ViewId,
    val audio: AudioState,
    val commands: List<HaftCommand<GuiCommandType>>
)

fun isGuiActive(state: ClientState): Boolean = pauseViews.contains(state.view)

fun newClientState(platform: Platform, inputConfig: GameInputConfig, audioConfig: AudioConfig) =
    ClientState(
        input = newInputState(inputConfig),
        bloomState = newBloomState(),
        audio = newAudioState(platform.audio, audioConfig.soundVolume),
        view = ViewId.none,
        commands = listOf()
    )

//fun loadTextResource(): TextResources {
//  val typeref = object : TypeReference<TextResources>() {}
//  return loadYamlResource("text/english.yaml", typeref)
//}

fun loadTextResource(mapper: TextResourceMapper): TextResources {
  return Text.values().map { Pair(it, mapper(it)) }.associate { it }
}

fun gatherFontSets() = loadFontSets(baseFonts, textStyles)

class Client(val platform: Platform, val renderer: Renderer) {
  val screens: List<Screen> = (1..maxPlayerCount).map { Screen(it) }
  val textResources: TextResources = loadTextResource(englishTextResources)
  val soundLibrary: SoundLibrary = loadSounds(platform.audio)
  fun getWindowInfo() = platform.display.getInfo()

  init {
    setGlobalFonts(renderer.fonts)
    platform.audio.start(50)
  }

  fun shutdown() {
    platform.audio.stop()
  }
}

fun updateMousePointerVisibility(platform: Platform) {
  val windowHasFocus = platform.display.hasFocus()
  platform.input.isMouseVisible(!windowHasFocus)
}

fun applyClientCommands(client: Client, commands: UserCommands): (ClientState) -> ClientState = { state ->
  val c = commands.map { it.type }
  if (c.contains(GuiCommandType.quit)) {
    client.platform.process.close()
  }

  menuChangeView(commands)(state)
}

fun <T> getBinding(inputState: InputState, inputProfiles: Map<BloomId, InputProfile<T>>): BindingSource<T> = { event ->
  val playerDevice = inputState.deviceMap[event.device]
  if (playerDevice != null) {
    val playerProfile = inputState.playerProfiles[playerDevice.player]!!
    val profile = inputProfiles[playerProfile]!!
    val binding = profile.bindings.firstOrNull { it.device == playerDevice.device && it.trigger == event.index }
    if (binding != null)
      Pair(binding, playerDevice.player)
    else
      null
  } else
    null
}

fun getListenerPosition(deck: Deck): Vector3? {
  val player = deck.players.keys.firstOrNull()
  val body = deck.bodies[player]
  return body?.position
}

fun updateClientInput(client: Client): (ClientState) -> ClientState = { state ->
  val newDeviceStates = updateInputDeviceStates(client.platform.input, state.input.deviceStates)
  state.copy(
      input = state.input.copy(
          deviceStates = newDeviceStates
      )
  )
}

// So guiEvents are not persisted
fun pruneBag(bloomState: BloomState): BloomState {
  return bloomState.copy(
      bag = bloomState.bag.minus(guiEventsKey)
  )
}

val clientBloomModules: List<LogicModule> = listOf()

fun updateClient(client: Client, players: List<Int>, box: Box): (ClientState) -> ClientState = { clientState ->
  //  updateMousePointerVisibility(client.platform)
  val bindingContext = bindingContext(clientState)
  val getBinding = getBinding(clientState.input, clientState.input.guiInputProfiles)
  val strokes = clientCommandStrokes[bindingContext]!!
  val deviceStates = clientState.input.deviceStates
  val commands = mapEventsToCommands(deviceStates, strokes, getBinding)

  val bloomInputState = newBloomInputState(deviceStates.last())
      .copy(events = haftToBloom(commands))
  val (bloomState, bloomEvents) = updateBloomState(clientBloomModules, box, pruneBag(clientState.bloomState), bloomInputState)

  val guiCommands = guiEvents(bloomState.bag)
      .filter { it.type == GuiEventType.command }
      .map { simpleCommand(it.data as GuiCommandType, players.first()) }

  val allCommands = commands
      .plus(guiCommands)

  val newClientState = pipe(
      { state ->
        state.copy(
            input = updateInputState(deviceStates, state.input),
            bloomState = bloomState,
            commands = allCommands
        )
      },
      // This needs to happen after applying updateBloomState to override flower state settings
      applyClientCommands(client, allCommands)
  )(clientState)

  newClientState
}

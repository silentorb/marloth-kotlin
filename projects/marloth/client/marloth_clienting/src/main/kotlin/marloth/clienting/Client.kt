package marloth.clienting

import haft.*
import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.loadSounds
import marloth.clienting.audio.updateAppStateAudio
import marloth.clienting.gui.*
import marloth.clienting.input.*
import marloth.clienting.textResources.englishTextResources
import mythic.aura.AudioState
import mythic.aura.SoundLibrary
import mythic.aura.newAudioState
import mythic.bloom.BloomId
import mythic.bloom.BloomState
import mythic.bloom.next.Box
import mythic.bloom.next.LogicModule
import mythic.bloom.next.newBloomState
import mythic.bloom.updateBloomState
import mythic.drawing.setGlobalFonts
import mythic.ent.pipe
import mythic.platforming.Platform
import mythic.spatial.Vector3
import mythic.typography.loadFontSets
import newBloomInputState
import rendering.Renderer
import scenery.Screen
import scenery.enums.Text
import simulation.main.Deck
import simulation.main.World
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

fun applyCommandsToExternalSystem(client: Client): (ClientState) -> ClientState = { state ->
  val c = state.commands.map { it.type }
  if (c.contains(GuiCommandType.quit)) {
    client.platform.process.close()
  }

  state
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
  val deviceStates = updateInputDeviceStates(client.platform.input, state.input.deviceStates)
  state.copy(
      input = state.input.copy(
          deviceStates = deviceStates,
          deviceMap = updateDeviceMapWithNewPlayers(deviceStates)(state.input.deviceMap)
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

val updateClientInputCommands: (ClientState) -> ClientState = { clientState ->
  clientState.copy(
      commands = gatherInputCommands(clientState.input, bindingContext(clientState))
  )
}

fun updateClientBloomState(client: Client, players: List<Int>, box: Box): (ClientState) -> ClientState = { clientState ->
  val deviceStates = clientState.input.deviceStates
  val commands = clientState.commands

  val bloomInputState = newBloomInputState(deviceStates.last())
      .copy(events = haftToBloom(commands))
  val (bloomState, bloomEvents) = updateBloomState(clientBloomModules, box, pruneBag(clientState.bloomState), bloomInputState)

  val commandsFromGui = guiEvents(bloomState.bag)
      .filter { it.type == GuiEventType.command }
      .map { simpleCommand(it.data as GuiCommandType, players.first()) }

  clientState.copy(
      bloomState = bloomState,
      commands = commands.plus(commandsFromGui)
  )
}

fun updateClient(client: Client, players: List<Int>, worlds: List<World>, box: Box): (ClientState) -> ClientState =
    pipe(
        updateClientInput(client),
        updateClientInputCommands,
        updateClientBloomState(client, players, box),
        applyCommandsToExternalSystem(client),
        menuChangeView,
        updateAppStateAudio(client, worlds)
    )

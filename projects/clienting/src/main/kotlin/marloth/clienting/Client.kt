package marloth.clienting

import silentorb.mythic.haft.HaftCommand
import silentorb.mythic.haft.simpleCommand
import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.updateClientAudio
import marloth.clienting.menus.*
import marloth.clienting.input.*
import marloth.clienting.textResources.englishTextResources
import marloth.definition.ClientDefinitions
import silentorb.mythic.aura.AudioState
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.LogicModule
import silentorb.mythic.bloom.next.newBloomState
import silentorb.mythic.bloom.updateBloomState
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import silentorb.mythic.platforming.Platform
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.typography.loadFontSets
import silentorb.mythic.bloom.input.newBloomInputState
import silentorb.mythic.lookinglass.Renderer
import marloth.scenery.enums.Text
import simulation.main.Deck
import simulation.main.World
import silentorb.mythic.bloom.input.updateInputDeviceStates
import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.lookinglass.mapAnimationInfo
import silentorb.mythic.lookinglass.texturing.AsyncTextureLoader

const val maxPlayerCount = 4

typealias PlayerViews = Map<Id, ViewId?>

data class ClientState(
    val input: InputState,
    val bloomState: BloomState,
    val playerViews: PlayerViews,
    val audio: AudioState,
    val commands: List<HaftCommand>,

    // Players could be extracted from the world deck except the world does not care about player order.
    // Player order is only a client concern, and only for local multiplayer.
    // The only reason for this players list is to keep track of the client player order.
    val players: List<Id>
)

fun isMenuActive(state: ClientState): (Id) -> Boolean = { player ->
  state.playerViews[player] ?: ViewId.none != ViewId.none
}

//fun isAnyGuiActive(state: ClientState): Boolean = pauseViews.any { state.playerViews.values.contains(it) }

fun newClientState(platform: Platform, inputConfig: GameInputConfig, audioConfig: AudioConfig) =
    ClientState(
        input = newInputState(inputConfig),
        bloomState = newBloomState(),
        audio = newAudioState(audioConfig.soundVolume),
        playerViews = mapOf(),
        commands = listOf(),
        players = listOf()
    )

//fun loadTextResource(): TextResources {
//  val typeref = object : TypeReference<TextResources>() {}
//  return loadYamlResource("text/english.yaml", typeref)
//}

fun loadTextResource(mapper: TextResourceMapper): TextResources {
  return Text.values().map { Pair(it, mapper(it)) }.associate { it }
}

fun gatherFontSets() = loadFontSets(baseFonts, textStyles)

data class Client(
    val platform: Platform,
    val renderer: Renderer,
    val soundLibrary: SoundLibrary,
    val textureLoader: AsyncTextureLoader,
    val textResources: TextResources = loadTextResource(englishTextResources),
    val customBloomResources: Map<String, Any>
) {
  fun getWindowInfo() = platform.display.getInfo()

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

fun getListenerPosition(deck: Deck): Vector3? {
  val player = deck.players.keys.firstOrNull()
  val body = deck.bodies[player]
  return body?.position
}

fun updateClientInput(client: Client): (ClientState) -> ClientState = { clientState ->
  val deviceStates = updateInputDeviceStates(client.platform.input, clientState.input.deviceStates)
  clientState.copy(
      input = clientState.input.copy(
          deviceStates = deviceStates
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

fun updateClientInputCommands(): (ClientState) -> ClientState = { clientState ->
  clientState.copy(
      commands = gatherInputCommands(clientState.input, clientState.playerViews)
  )
}

fun updateClientBloomState(box: Box): (ClientState) -> ClientState = { clientState ->
  val deviceStates = clientState.input.deviceStates
  val commands = clientState.commands

  val bloomInputState = newBloomInputState(deviceStates.last())
      .copy(events = haftToBloom(commands))
  val (bloomState, _) = updateBloomState(clientBloomModules, box, pruneBag(clientState.bloomState), bloomInputState)

  val commandsFromGui = guiEvents(bloomState.bag)
      .filter { it.type == GuiEventType.command }
      .map { simpleCommand(it.data as GuiCommandType, clientState.players.first()) } // TODO: This needs to be changed for multiplayer

  clientState.copy(
      bloomState = bloomState,
      commands = commands.plus(commandsFromGui)
  )
}

fun updateClient(client: Client, worlds: List<World>, box: Box): (ClientState) -> ClientState = { clientState ->
  pipe(
      updateClientInput(client),
      updateClientInputCommands(),
      updateClientBloomState(box),
      applyCommandsToExternalSystem(client),
      updateClientCurrentMenus,
      updateClientAudio(clientState, client, worlds)
  )(clientState)
}

fun definitionsFromClient(client: Client): ClientDefinitions =
    ClientDefinitions(
        animations = mapAnimationInfo(client.renderer.armatures),
        lightAttachments = gatherMeshLights(client.renderer.meshes),
        soundDurations = client.soundLibrary.mapValues { it.value.duration }
    )

package marloth.clienting

import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.updateClientAudio
import marloth.clienting.input.*
import marloth.clienting.menus.*
import marloth.clienting.rendering.MeshLoadingState
import marloth.definition.texts.englishTextResources
import marloth.definition.misc.ClientDefinitions
import silentorb.mythic.aura.AudioState
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.input.newBloomInputState
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.LogicModule
import silentorb.mythic.bloom.next.newBloomState
import silentorb.mythic.bloom.updateBloomState
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.*
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.mapAnimationInfo
import silentorb.mythic.lookinglass.texturing.TextureLoadingState
import silentorb.mythic.platforming.Platform
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.typography.loadFontSets
import simulation.main.Deck
import simulation.main.World

const val maxPlayerCount = 4

typealias PlayerViews = Map<Id, ViewId?>

data class ClientState(
    val audio: AudioState,
    val bloomStates: Map<Id, BloomState>,
    val commands: List<HaftCommand>,
    val input: InputState,
    val playerViews: PlayerViews,

    // Players could be extracted from the world deck except the world does not care about player order.
    // Player order is only a client concern, and only for local multiplayer.
    // The only reason for this players list is to keep track of the client player order.
    val players: List<Id>
)

fun isMenuActive(state: ClientState): (Id) -> Boolean = { player ->
  state.playerViews[player] ?: ViewId.none != ViewId.none
}

fun getPlayerBloomState(bloomStates: Map<Id, BloomState>, player: Id): BloomState =
    bloomStates.getOrElse(player) { newBloomState() }

//fun isAnyGuiActive(state: ClientState): Boolean = pauseViews.any { state.playerViews.values.contains(it) }

fun newClientState(platform: Platform, inputConfig: GameInputConfig, audioConfig: AudioConfig) =
    ClientState(
        input = newInputState(inputConfig),
        bloomStates = mapOf(),
        audio = newAudioState(audioConfig.soundVolume),
        playerViews = mapOf(),
        commands = listOf(),
        players = listOf()
    )

//fun loadTextResource(): TextResources {
//  val typeref = object : TypeReference<TextResources>() {}
//  return loadYamlResource("text/english.yaml", typeref)
//}

fun gatherFontSets() = loadFontSets(baseFonts, textStyles)

data class Client(
    val platform: Platform,
    val renderer: Renderer,
    val soundLibrary: SoundLibrary,
    val meshLoadingState: MeshLoadingState,
    val textureLoadingState: TextureLoadingState,
    val textResources: TextResources = englishTextResources,
    val customBloomResources: Map<String, Any>
) {
  fun getWindowInfo() = platform.display.getInfo()

  fun shutdown() {
    platform.audio.stop()
  }
}

fun updateMousePointerVisibility(platform: Platform) {
  if (!getDebugBoolean("DISABLE_MOUSE")) {
    val windowHasFocus = platform.display.hasFocus()
    platform.input.isMouseVisible(!windowHasFocus)
  }
}

fun applyCommandsToExternalSystem(client: Client, commands: HaftCommands) {
  val c = commands.map { it.type }
  if (c.contains(GuiCommandType.quit)) {
    client.platform.process.close()
  }
}

fun getListenerPosition(deck: Deck): Vector3? {
  val player = deck.players.keys.firstOrNull()
  val body = deck.bodies[player]
  return body?.position
}

// So guiEvents are not persisted
fun pruneBag(bloomState: BloomState): BloomState {
  return bloomState.copy(
      bag = bloomState.bag.minus(guiEventsKey)
  )
}

val clientBloomModules: List<LogicModule> = listOf()

fun updateClientBloomStates(boxes: List<Box>, bloomStates: Map<Id, BloomState>, deviceStates: List<InputDeviceState>, commands: HaftCommands, players: List<Id>): Map<Id, BloomState> {
  val baseBloomInputState = newBloomInputState(deviceStates.last())

  return players.zip(boxes) { player, box ->
    val playerCommands = commands.filter { it.target == player }
    val bloomInputState = baseBloomInputState.copy(events = haftToBloom(playerCommands))
    val previousBloomState = getPlayerBloomState(bloomStates, player)
    val (bloomState, _) = updateBloomState(clientBloomModules, box, pruneBag(previousBloomState), bloomInputState)
    Pair(player, bloomState)
  }
      .associate { it }
}

fun updateClient(client: Client, worlds: List<World>, boxes: List<Box>, clientState: ClientState): ClientState {
  updateMousePointerVisibility(client.platform)
  val deviceStates = updateInputDeviceStates(client.platform.input, clientState.input.deviceStates)
  val input = clientState.input.copy(
      deviceStates = deviceStates
  )
  val initialCommands = gatherInputCommands(clientState.input, clientState.playerViews)
  val bloomStates = updateClientBloomStates(boxes, clientState.bloomStates, clientState.input.deviceStates, initialCommands, clientState.players)
  val commandsFromGui = bloomStates.flatMap { (player, bloomState) ->
    guiEvents(bloomState.bag)
        .filter { it.type == GuiEventType.command }
        .map { simpleCommand(it.data as GuiCommandType, 0, player) }
  }

  val commands = initialCommands.plus(commandsFromGui)
  applyCommandsToExternalSystem(client, commands)
  val playerViews = updateClientCurrentMenus(worlds.last().deck, commands, clientState.players, clientState.playerViews)

  return clientState.copy(
      audio = updateClientAudio(client, worlds, clientState.audio),
      input = input,
      bloomStates = bloomStates,
      commands = commands,
      playerViews = playerViews
  )
}

fun definitionsFromClient(client: Client): ClientDefinitions =
    ClientDefinitions(
        animations = mapAnimationInfo(client.renderer.armatures),
        lightAttachments = gatherMeshLights(client.renderer.meshes),
        soundDurations = client.soundLibrary.mapValues { it.value.duration }
    )

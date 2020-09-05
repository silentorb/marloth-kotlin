package marloth.clienting

import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.updateClientAudio
import marloth.clienting.input.*
import marloth.clienting.menus.*
import marloth.clienting.rendering.MeshLoadingState
import marloth.clienting.rendering.marching.MarchingState
import marloth.clienting.rendering.marching.newMarchingState
import marloth.definition.misc.ClientDefinitions
import marloth.definition.texts.englishTextResources
import silentorb.mythic.aura.AudioState
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.input.newBloomInputState
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.LogicModule
import silentorb.mythic.bloom.next.emptyLogic
import silentorb.mythic.bloom.next.newBloomState
import silentorb.mythic.bloom.updateBloomState
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.fathom.misc.ModelFunction
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

const val canvasRendererKey = "renderer"

typealias PlayerViews = Map<Id, ViewId?>

data class MarlothBloomState(
    val bloom: BloomState,
    val menuStack: MenuStack,
    val view: ViewId?,
    val menuFocusIndex: Int
)

typealias MarlothBloomStateMap = Map<Id, MarlothBloomState>

data class ClientState(
    val audio: AudioState,
    val bloomStates: Map<Id, MarlothBloomState>,
    val commands: List<HaftCommand>,
    val input: InputState,
    val marching: MarchingState,

    // Players could be extracted from the world deck except the world does not care about player order.
    // Player order is only a client concern, and only for local multiplayer.
    // The only reason for this players list is to keep track of the client player order.
    val players: List<Id>
)

//fun isMenuActive(state: ClientState): (Id) -> Boolean = { player ->
//  state.playerViews[player] ?: ViewId.none != ViewId.none
//}

fun newMarlothBloomState() =
    MarlothBloomState(
        bloom = newBloomState(),
        menuStack = listOf(),
        view = null,
        menuFocusIndex = 0
    )

fun getPlayerBloomState(bloomStates: Map<Id, MarlothBloomState>, player: Id): MarlothBloomState =
    bloomStates.getOrElse(player) { newMarlothBloomState() }

//fun isAnyGuiActive(state: ClientState): Boolean = pauseViews.any { state.playerViews.values.contains(it) }

fun newClientState(platform: Platform, inputConfig: GameInputConfig, audioConfig: AudioConfig) =
    ClientState(
        input = newInputState(inputConfig),
        bloomStates = mapOf(),
        audio = newAudioState(audioConfig.soundVolume),
        commands = listOf(),
        players = listOf(),
        marching = newMarchingState()
    )

fun playerViews(client: ClientState): Map<Id, ViewId?> =
    client.bloomStates.mapValues { it.value.view }

//fun loadTextResource(): TextResources {
//  val typeref = object : TypeReference<TextResources>() {}
//  return loadYamlResource("text/english.yaml", typeref)
//}

fun gatherFontSets() = loadFontSets(baseFonts, textStyles)

data class Client(
    val platform: Platform,
    val renderer: Renderer,
    val soundLibrary: SoundLibrary,
    val meshLoadingState: MeshLoadingState? = null,
    val impModels: Map<String, ModelFunction>,
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

val clientBloomModule: LogicModule = emptyLogic

fun updateClientBloomStates(boxes: List<Box>, bloomStates: Map<Id, MarlothBloomState>, deviceStates: List<InputDeviceState>, commands: HaftCommands, players: List<Id>): Map<Id, BloomState> {
  val baseBloomInputState = newBloomInputState(deviceStates.last())

  return players.zip(boxes) { player, box ->
    val playerCommands = commands.filter { it.target == player }
    val bloomInputState = baseBloomInputState.copy(events = haftToBloom(playerCommands))
    val previousBloomState = getPlayerBloomState(bloomStates, player)
    val (bloomState, _) = updateBloomState(clientBloomModule, box, pruneBag(previousBloomState.bloom), bloomInputState)
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
  val initialCommands = gatherInputCommands(clientState.input, playerViews(clientState))
  val bloomStates = updateClientBloomStates(boxes, clientState.bloomStates, clientState.input.deviceStates, initialCommands, clientState.players)
  val commandsFromGui = bloomStates
      .flatMap { (player, bloomState) ->
        guiEvents(bloomState.bag)
            .mapNotNull {
              if (it.client != null)
                simpleCommand(it.client.type, 0, player, it.client.data)
              else
                null
            }
      }

  val commands = initialCommands.plus(commandsFromGui)
  applyCommandsToExternalSystem(client, commands)
  val playerViews = updateClientCurrentMenus(worlds.last().deck, clientState.bloomStates, commands, clientState.players)

  return clientState.copy(
      audio = updateClientAudio(client, worlds, clientState.audio),
      input = input,
      bloomStates = playerViews.mapValues { it.value.copy(bloom = bloomStates[it.key]!!) },
      commands = commands
  )
}

fun definitionsFromClient(client: Client): ClientDefinitions =
    ClientDefinitions(
        animations = mapAnimationInfo(client.renderer.armatures),
        lightAttachments = gatherMeshLights(client.renderer.meshes),
        soundDurations = client.soundLibrary.mapValues { it.value.duration }
    )

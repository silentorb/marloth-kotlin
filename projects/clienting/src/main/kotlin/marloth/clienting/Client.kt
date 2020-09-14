package marloth.clienting

import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.updateClientAudio
import marloth.clienting.input.*
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.logic.getMenuEvents
import marloth.clienting.gui.menus.logic.updateGuiStates
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.GuiEvent
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.ViewId
import marloth.clienting.rendering.MeshLoadingState
import marloth.clienting.rendering.marching.newMarchingState
import marloth.definition.misc.ClientDefinitions
import marloth.definition.texts.englishTextResources
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.bloom.*
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.haft.HaftCommands
import silentorb.mythic.haft.simpleCommand
import silentorb.mythic.haft.updateInputDeviceStates
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.mapAnimationInfo
import silentorb.mythic.lookinglass.texturing.TextureLoadingState
import silentorb.mythic.platforming.Platform
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.loadFontSets
import simulation.main.Deck
import simulation.main.World

const val maxPlayerCount = 4

fun newMarlothBloomState() =
    GuiState(
        bloom = newBloomState(),
        menuStack = listOf(),
        view = null,
        menuFocusIndex = 0
    )

fun getPlayerBloomState(guiStates: Map<Id, GuiState>, player: Id): GuiState =
    guiStates.getOrElse(player) { newMarlothBloomState() }

fun newClientState(inputConfig: GameInputConfig, audioConfig: AudioConfig) =
    ClientState(
        input = newInputState(inputConfig),
        guiStates = mapOf(),
        audio = newAudioState(audioConfig.soundVolume),
        commands = listOf(),
        players = listOf(),
        marching = newMarchingState(),
        events = listOf()
    )

fun playerViews(client: ClientState): Map<Id, ViewId?> =
    client.guiStates.mapValues { it.value.view }

//fun loadTextResource(): TextResources {
//  val typeref = object : TypeReference<TextResources>() {}
//  return loadYamlResource("text/english.yaml", typeref)
//}

fun gatherFontSets() = loadFontSets(baseFonts, TextStyles)

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
    platform.display.shutdown()
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

typealias PlayerBoxes = Map<Id, List<OffsetBox>>

fun flattenToPlayerBoxes(boxes: Map<Id, Box>): PlayerBoxes =
    boxes.mapValues { flattenAllBoxes(OffsetBox(it.value)).filter(::hasAttributes) }

fun updateClient(
    client: Client,
    options: AppOptions,
    worlds: List<World>,
    playerBoxes: PlayerBoxes,
    playerBloomDefinitions: Map<Id, BloomDefinition>,
    clientState: ClientState): ClientState {
  updateMousePointerVisibility(client.platform)
  val deviceStates = updateInputDeviceStates(client.platform.input, clientState.input.deviceStates)
  val input = clientState.input.copy(
      deviceStates = deviceStates
  )
  val initialCommands = gatherInputCommands(clientState.input, input, playerViews(clientState))
  val mousePosition = clientState.input.deviceStates.first().mousePosition.toVector2i()
  val menuEvents = playerBoxes
      .mapValues { (player, boxes) ->
        val state = clientState.guiStates[player]
        if (state != null) {
          val hoverBoxes = getHoverBoxes(mousePosition, boxes)
          getMenuEvents(boxes, hoverBoxes, initialCommands.filter { it.target == player })
        } else
          listOf()
      }

  val menuClientCommands = menuEvents
      .flatMap { (player, events) ->
        events
            .filterIsInstance<GuiEvent>()
            .map { simpleCommand(it.type, 0, player, it.data) }
      }

  val events = menuEvents.values.flatten()

  val commands = initialCommands.plus(menuClientCommands)
  applyCommandsToExternalSystem(client, commands)
  val nextGuiStates = updateGuiStates(
      options,
      clientState.guiStates,
      playerBloomDefinitions,
      mousePosition,
      playerBoxes,
      commands,
      events.filterIsInstance<ClientEvent>()
  )

  return clientState.copy(
      audio = updateClientAudio(client, worlds, clientState.audio),
      input = input,
      guiStates = nextGuiStates,
      commands = commands,
      events = events
  )
}

fun definitionsFromClient(client: Client): ClientDefinitions =
    ClientDefinitions(
        animations = mapAnimationInfo(client.renderer.armatures),
        lightAttachments = gatherMeshLights(client.renderer.meshes),
        soundDurations = client.soundLibrary.mapValues { it.value.duration }
    )

package marloth.clienting

import marloth.clienting.audio.AudioConfig
import marloth.clienting.audio.updateClientAudio
import marloth.clienting.editing.editorFonts
import marloth.clienting.editing.expandDefaultWorldGraph
import marloth.clienting.editing.newEditor
import marloth.clienting.editing.updateEditingActive
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.baseFonts
import marloth.clienting.gui.menus.logic.commandsToClientEvents
import marloth.clienting.gui.menus.logic.eventsFromGuiState
import marloth.clienting.gui.menus.logic.getMenuItemEvents
import marloth.clienting.gui.menus.logic.updateGuiState
import marloth.clienting.input.GameInputConfig
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.gatherInputCommands
import marloth.clienting.input.newInputState
import marloth.definition.misc.ClientDefinitions
import marloth.definition.texts.englishTextResources
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.aura.newAudioState
import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.old.flattenAllBoxes
import silentorb.mythic.bloom.old.getHoverBoxes
import silentorb.mythic.bloom.old.hasAttributes
import silentorb.mythic.bloom.old.newBloomState
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.editing.*
import silentorb.mythic.editing.updating.prepareEditorUpdate
import silentorb.mythic.editing.updating.updateEditor
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.updateInputDeviceStates
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.handleCommands
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.lookinglass.mapAnimationInfo
import silentorb.mythic.lookinglass.texturing.TextureLoadingState
import silentorb.mythic.platforming.DisplayMode
import silentorb.mythic.platforming.Platform
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.loadFontSets
import simulation.main.Deck
import simulation.main.World

const val maxPlayerCount = 4

fun newMarlothBloomState(primarydeviceMode: DeviceMode) =
    GuiState(
        bloom = newBloomState(),
        menuStack = listOf(),
        view = null,
        menuFocusIndex = 0,
        primarydeviceMode = primarydeviceMode,
    )

fun newClientState(
    textLibrary: TextResourceMapper,
    inputConfig: GameInputConfig,
    audioConfig: AudioConfig,
    displayModes: List<DisplayMode>
) =
    ClientState(
        input = newInputState(inputConfig),
        guiStates = mapOf(),
        audio = newAudioState(audioConfig.soundVolume),
        commands = listOf(),
        players = listOf(),
        events = listOf(),
        displayModes = displayModes,
        editor = initialEditor(textLibrary)
    )

fun playerViews(client: ClientState): Map<Id, ViewId?> =
    client.guiStates.mapValues { it.value.view }

fun gatherFontSets() = loadFontSets(baseFonts, TextStyles)

data class Client(
    val platform: Platform,
    val renderer: Renderer,
    val soundLibrary: SoundLibrary,
    val textureLoadingState: TextureLoadingState,
    val textResources: TextResources = englishTextResources,
    val customBloomResources: Map<String, Any>
) {
  fun getWindowInfo() = platform.display.getInfo()

  fun shutdown() {
    closeImGui()
    platform.display.shutdown()
    platform.audio.stop()
  }
}

fun updateMousePointerVisibility(platform: Platform, viewId: ViewId?, isEditorActive: Boolean) {
  if (!getDebugBoolean("DISABLE_MOUSE")) {
    val windowHasFocus = platform.display.hasFocus()
    platform.input.setMouseVisibility(!windowHasFocus || viewId != null || isEditorActive)
  } else {
    platform.input.setMouseVisibility(true)
  }
}

fun applyCommandsToExternalSystem(client: Client, events: List<ClientEvent>) {
  val eventTypes = events.map { it.type }
  if (eventTypes.contains(GuiCommandType.quit)) {
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

fun gatherUserEvents(
    options: AppOptions,
    clientState: ClientState,
    playerBoxes: PlayerBoxes,
    mousePosition: Vector2i,
    commands: Commands
): List<EventUnion> {
  val menuEvents = playerBoxes
      .mapValues { (player, boxes) ->
        val state = clientState.guiStates[player]
        if (state != null) {
          val hoverBoxes = getHoverBoxes(mousePosition, boxes)
          val playerCommands = commands.filter { it.target == player }
          val input = Input(
              commands = playerCommands,
              mousePosition = mousePosition,
          )
          getMenuItemEvents(boxes, hoverBoxes, playerCommands).map { event ->
            if (event is ClientEvent)
              event.copy(
                  target = player
              )
            else
              event
          } +
              (commandsToClientEvents(options, state, playerCommands) + eventsFromGuiState(state))
                  .map { event ->
                    event.copy(
                        target = player
                    )
                  } +
              commandsFromBoxes(boxes, input).map { it.copy(target = player) }
        } else
          listOf()
      }

  return menuEvents.values.flatten()
}

fun getEditorEvents(editor: Editor) = handleCommands<List<ClientEvent>> { command, events ->
  when (command.type) {
    EditorCommands.playGame -> events + ClientEvent(GuiCommandType.newGame)
    EditorCommands.playScene -> events + ClientEvent(GuiCommandType.newGame, editor.state.graph)
    else -> events
  }
}

fun updateClient(
    client: Client,
    textLibrary: TextResourceMapper,
    options: AppOptions,
    worlds: List<World>,
    playerBoxes: PlayerBoxes,
    playerBloomDefinitions: Map<Id, BloomDefinition>,
    clientState: ClientState
): ClientState {
  updateMousePointerVisibility(client.platform, clientState.guiStates[clientState.players.firstOrNull()]?.view, clientState.isEditorActive)

  val deviceStates = updateInputDeviceStates(client.platform.input, clientState.input.deviceStates)
  val input = clientState.input.copy(
      deviceStates = deviceStates
  )
  val commands = gatherInputCommands(clientState.input, input, playerViews(clientState))
  val mousePosition = clientState.input.deviceStates.first().mousePosition.toVector2i()
  val events = gatherUserEvents(options, clientState, playerBoxes, mousePosition, commands)
  val commands2 = commands + events.filterIsInstance<Command>()

  applyCommandsToExternalSystem(client, commands2)
  val nextGuiStates = if (clientState.isEditorActive)
    clientState.guiStates
  else
    playerBloomDefinitions
        .mapValues { (player, bloomDefinition) ->
          updateGuiState(
              options,
              worlds.lastOrNull()?.deck,
              clientState.guiStates,
              mousePosition,
              playerBoxes,
              commands2,
              player,
              bloomDefinition
          )
        }

  val previousEditor = clientState.editor
  val windowInfo = client.getWindowInfo()
  val (nextEditor, editorEvents1) = if (clientState.isEditorActive) {
    ensureImGuiIsInitialized(editorFonts, windowInfo.id)
    val editor = previousEditor ?: newEditor(textLibrary)
    val editorCommands = prepareEditorUpdate(deviceStates, editor)
    val editorEvents = getEditorEvents(editor)(editorCommands, listOf())
    updateEditor(deviceStates, editorCommands, editor) to editorEvents
  } else
    previousEditor to listOf()

  checkSaveEditor(clientState.editor, nextEditor)
  val nextIsEditorActive = updateEditingActive(commands + editorEvents1, clientState.isEditorActive)
  val editorEvents2 = if (!nextIsEditorActive && clientState.isEditorActive && nextEditor != null)
    listOf(ClientEvent(ClientEventType.setWorldGraph, expandDefaultWorldGraph(nextEditor)))
  else
    listOf()

  return clientState.copy(
      audio = updateClientAudio(client, worlds, clientState.audio),
      input = input,
      guiStates = nextGuiStates,
      commands = commands2 + editorEvents1 + editorEvents2,
      events = events,
      isEditorActive = nextIsEditorActive,
      editor = nextEditor,
  )
}

fun definitionsFromClient(client: Client): ClientDefinitions =
    ClientDefinitions(
        animations = mapAnimationInfo(client.renderer.armatures),
        lightAttachments = gatherMeshLights(client.renderer.meshes),
        soundDurations = client.soundLibrary.mapValues { it.value.duration },
        meshShapeMap = getMeshShapes(client.renderer)
    )

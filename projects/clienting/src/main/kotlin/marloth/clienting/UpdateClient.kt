package marloth.clienting

import marloth.clienting.audio.updateClientAudio
import marloth.clienting.editing.*
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.EventUnion
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.logic.commandsToClientEvents
import marloth.clienting.gui.menus.logic.eventsFromGuiState
import marloth.clienting.gui.menus.logic.getMenuItemEvents
import marloth.clienting.gui.menus.logic.updateGuiState
import marloth.clienting.input.DebugCommands
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.gatherInputCommands
import marloth.definition.misc.ClientDefinitions
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.Input
import silentorb.mythic.bloom.OffsetBox
import silentorb.mythic.bloom.commandsFromBoxes
import silentorb.mythic.bloom.old.flattenAllBoxes
import silentorb.mythic.bloom.old.getHoverBoxes
import silentorb.mythic.bloom.old.hasAttributes
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.debugging.toggleDebugBoolean
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.EditorCommands
import silentorb.mythic.editing.checkSaveEditor
import silentorb.mythic.editing.ensureImGuiIsInitialized
import silentorb.mythic.editing.updating.prepareEditorUpdate
import silentorb.mythic.editing.updating.updateEditor
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.updateInputDeviceStates
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.handleCommands
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.lookinglass.mapAnimationInfo
import silentorb.mythic.platforming.Platform
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.toVector2i
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Factions


fun updateMousePointerVisibility(platform: Platform, clientState: ClientState) {
  if (!getDebugBoolean("DISABLE_MOUSE")) {
    val windowHasFocus = platform.display.hasFocus()
    val view = clientState.guiStates[clientState.players.firstOrNull()]?.view
    val isEditing = clientState.isEditorActive && !(clientState.editor?.flyThrough ?: false)
    platform.input.setMouseVisibility(!windowHasFocus || view != null || isEditing)
  } else {
    platform.input.setMouseVisibility(true)
  }
}

fun toggleDebugBooleanByNumber(number: Int) {
  val key = getDebugString("TOGGLE_KEY_TARGET$number")
  if (key != null) {
    toggleDebugBoolean(key)
  }
}

val handleDebugToggleCommands = handleCommands<Any> { command, _ ->
  when (command.type) {
    DebugCommands.toggleValue1 -> toggleDebugBooleanByNumber(1)
    DebugCommands.toggleValue2 -> toggleDebugBooleanByNumber(2)
    DebugCommands.toggleValue3 -> toggleDebugBooleanByNumber(3)
    DebugCommands.toggleValue4 -> toggleDebugBooleanByNumber(4)
  }
}

fun applyCommandsToExternalSystem(client: Client, events: List<ClientEvent>) {
  val eventTypes = events.map { it.type }
  if (eventTypes.contains(GuiCommandType.quit)) {
    client.platform.process.close()
  }

  if (getDebugBoolean("ENABLE_DEBUGGING")) {
    handleDebugToggleCommands(events, 0)
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
            if (event is ClientEvent && event.target == null)
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
    EditorCommands.playScene -> events + ClientEvent(GuiCommandType.newGame, editor.persistentState.graph)
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
    clientState: ClientState,
    externalCommands: Commands
): ClientState {
  updateMousePointerVisibility(client.platform, clientState)

  val deviceStates = updateInputDeviceStates(client.platform.input, clientState.input.deviceStates)
  val input = clientState.input.copy(
      deviceStates = deviceStates
  )
  val commands = gatherInputCommands(clientState.input, input, playerViews(clientState))
  val mousePosition = clientState.input.deviceStates.first().mousePosition.toVector2i()
  val events = gatherUserEvents(options, clientState, playerBoxes, mousePosition, commands)
  val commands2 = commands + events.filterIsInstance<Command>() +
      listOfNotNull(
          if (worlds.size > 1 && worlds.last().global.gameOver?.winningFaction == Factions.misfits &&
              worlds.dropLast(1).last().global.gameOver?.winningFaction != Factions.misfits)
            Command(ClientEventType.navigate, ViewId.victory, worlds.last().deck.players.keys.first())
          else
            null
      )

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
              commands2 + externalCommands,
              player,
              bloomDefinition
          )
        }

  val previousEditor = clientState.editor
  val windowInfo = client.getWindowInfo()
  val (nextEditor, editorEvents1) = if (clientState.isEditorActive) {
    ensureImGuiIsInitialized(editorFonts, windowInfo.id)
    val editor = previousEditor ?: newEditor(textLibrary, client.renderer.meshes.keys,
        getMeshShapes(client.renderer), getClientTextures(client))
    val editorCommands = prepareEditorUpdate(deviceStates, editor)
    val editorEvents = getEditorEvents(editor)(editorCommands, listOf())
    val editorWithWorld = updateEditorSyncing(worlds.lastOrNull(), editor)
    updateEditor(deviceStates, editorCommands, editorWithWorld) to editorEvents
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

package marloth.clienting.editing

import generation.general.BlockGrid
import marloth.clienting.Client
import marloth.clienting.getEditorEvents
import marloth.clienting.input.DeveloperCommands
import marloth.clienting.input.GuiCommandType
import marloth.clienting.rendering.characterMeshes
import marloth.clienting.rendering.characterPlacement
import marloth.definition.data.characterDefinitions
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshShapeMap
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.components.gizmoMenuToggleState
import silentorb.mythic.editing.general.Typeface
import silentorb.mythic.editing.main.*
import silentorb.mythic.editing.updating.prepareEditorUpdate
import silentorb.mythic.editing.updating.updateEditor
import silentorb.mythic.editing.updating.updateGraphStateAndHistory
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.ExpansionLibrary
import silentorb.mythic.ent.scenery.expandGraphInstances
import silentorb.mythic.ent.scenery.getAbsoluteNodeTransform
import silentorb.mythic.ent.scenery.getNodesWithAttribute
import silentorb.mythic.glowing.defaultTextureAttributes
import silentorb.mythic.haft.InputDeviceState
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.lookinglass.ResourceInfo
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.scenePropertiesSchema
import simulation.entities.DepictionType
import simulation.main.World
import simulation.misc.Entities
import simulation.misc.GameAttributes
import simulation.misc.distAttributes
import simulation.misc.marlothPropertiesSchema
import simulation.physics.CollisionGroups
import java.nio.file.Path

val editorFonts = listOf(
    Typeface(
        name = "EBGaramond",
        path = "fonts/EBGaramond-Regular.ttf",
        size = 16f
    )
)

const val worldResourcePath = "world"

var staticDebugBlockGrid: BlockGrid? = null

fun getMarlothEditorAttributes(): List<String> =
    commonEditorAttributes() + reflectProperties(GameAttributes) + distAttributes

fun marlothGraphSchema() =
    scenePropertiesSchema() + marlothPropertiesSchema()

val marlothEditorProperties =
    commonPropertyDefinitions() + marlothEditorPropertyDefinitions()

val marlothPropertiesSerialization =
    extractPropertiesSerialization(marlothEditorProperties)

fun marlothCollisionPresets(): Map<Int, String> =
    reflectPropertiesMap<Int>(CollisionGroups)
        .map { it.value to it.key }
        .associate { it }

fun editorLabel(parent: String, text: String): Graph = setOf(
    Entry("label", SceneProperties.type, CommonEditorAttributes.editorOnly),
    Entry("label", SceneProperties.parent, parent),
    Entry("label", SceneProperties.text3d, text),
)

fun spawners(): GraphLibrary = listOf(
    Entities.monsterSpawn
)
    .associateWith { key ->
      setOf(
          Entry(key, "", ""),
      )// + editorLabel(key, key)
    }

fun creatureDepiction(depictionType: String): EditorDepiction = { graph, node ->
  val initialTransform = getAbsoluteNodeTransform(graph, node)
  val transform = characterPlacement(initialTransform.translation(), 1f, initialTransform.rotation().z)
  val meshes = characterMeshes(depictionType)
  ElementGroup(
      meshes = meshes
          .map {
            MeshElement(
                id = 1L,
                mesh = it,
                transform = transform,
                location = initialTransform.translation()
            )
          },
  )
}

fun marlothEditorDepictions(): EditorDepictionMap = mapOf(
    Entities.monsterSpawn to creatureDepiction(DepictionType.hound)
)

fun newEditorGraphLibrary(textLibrary: TextResourceMapper): GraphLibrary =
    characterDefinitions()
        .mapValues { (key, definition) ->
          setOf(
              Entry(key, "", ""),
          ) + editorLabel(key, textLibrary(definition.name))
        } + spawners()

object PlaceholderTextures {
  val floor = "placeholderFloor"
  val wall = "placeholderWall"
  val ceiling = "placeholderCeiling"
}

fun marlothEditorMenus() =
    panelMenus() +
        mapOf(
            listOf(Contexts.viewport, Menus.display, MarlothEditorCommands.toggleBlockBounds) to MenuItem(
                label = "Block bounds",
                command = Command(EditorCommands.toggleGizmoVisibility, blockBoundsEnabledKey),
                getState = gizmoMenuToggleState(blockBoundsEnabledKey),
            ),
            listOf(Contexts.nodes, Menus.edit, MarlothEditorCommands.fillOccupiedCells) to MenuItem(
                label = "Fill cells",
                commandType = MarlothEditorCommands.fillOccupiedCells,
            ),
        )

fun expandGraph(editor: Editor, graph: Graph): Graph {
  val expansionLibrary = ExpansionLibrary(
      graphs = editor.graphLibrary,
      schema = editor.enumerations.schema,
  )
  return expandGraphInstances(expansionLibrary, graph)
}

fun marlothGraphEditors(): GraphEditors = mapOf(
    MarlothEditorCommands.fillOccupiedCells to { editor, command, graph ->
      val selection = getNodeSelection(editor)
      if (selection.size != 1)
        graph
      else {
        val expandedGraph = expandGraph(editor, graph)
        fillOccupied(editor.enumerations.resourceInfo.meshShapes, expandedGraph, selection.first())
      }
    }
)

fun newEditor(textLibrary: TextResourceMapper, meshes: Collection<String>, resourceInfo: ResourceInfo): Editor {
  val debugProjectPath = getDebugString("EDITOR_PROJECT_PATH")
  val projectPath = if (debugProjectPath != null)
    Path.of(debugProjectPath)
  else
    getUrlPath(worldResourcePath)

  return Editor(
      projectPath = projectPath,
      enumerations = EditorEnumerations(
          propertyDefinitions = marlothEditorProperties,
          propertiesSerialization = marlothPropertiesSerialization,
          schema = marlothGraphSchema(),
          attributes = getMarlothEditorAttributes(),
          meshes = meshes.toList().sorted(),
          resourceInfo = resourceInfo,
          collisionPresets = marlothCollisionPresets(),
          expanders = marlothExpanders(),
          depictions = marlothEditorDepictions(),
          menus = marlothEditorMenus(),
          gizmoPainters = listOf(blockBoundsPainter),
          graphEditors = defaultGraphEditors() + marlothGraphEditors(),
          graphTransform = ::applyCellDirectionOffsets,
      ),
      fileItems = loadProjectTree(projectPath, "world"),
      persistentState = loadEditorStateOrDefault(),
      graphLibrary = newEditorGraphLibrary(textLibrary),
  )
}

fun toggleEditor(current: EditingMode, toggled: EditingMode): EditingMode =
    if (current == toggled) EditingMode.none else toggled

fun updateEditingActive(commands: Commands, editingMode: EditingMode): EditingMode =
    when {
      commands.any { it.type == DeveloperCommands.editor } -> toggleEditor(editingMode, EditingMode.editor)
      commands.any { it.type == DeveloperCommands.aura } -> toggleEditor(editingMode, EditingMode.aura)
      commands.any { it.type == GuiCommandType.newGame } -> EditingMode.none
      else -> editingMode
    }

fun filterOutEditorOnlyNodes(graph: Graph): Graph {
  val editorOnlyNodes = getNodesWithAttribute(graph, CommonEditorAttributes.editorOnly)
  return graph
      .filter { !editorOnlyNodes.contains(it.source) }
      .toSet()
}

fun expandGameInstances(library: ExpansionLibrary, name: String): Graph =
    filterOutEditorOnlyNodes(expandGraphInstances(library, name))

fun loadWorldGraph(meshShapes: MeshShapeMap, name: String): Graph {
  val graphLibrary = loadMarlothGraphLibrary(marlothPropertiesSerialization)
  assert(graphLibrary.contains(name))
  val library = ExpansionLibrary(graphLibrary, marlothGraphSchema())
  return expandGameInstances(library, name)
}

const val defaultScene = "root"

fun mainScene() = getDebugString("DEFAULT_SCENE") ?: defaultScene

fun newExpansionLibrary(graphLibrary: GraphLibrary, meshShapes: Map<Key, Shape>) =
    ExpansionLibrary(graphLibrary, marlothGraphSchema())

fun expandWorldGraph(editor: Editor, scene: String): Graph {
  val graphLibrary = loadAllDependencies(editor, scene)
  val library = newExpansionLibrary(graphLibrary, editor.enumerations.resourceInfo.meshShapes)
  return expandGameInstances(library, scene)
}

fun expandDefaultWorldGraph(editor: Editor): Graph =
    expandWorldGraph(editor, mainScene())

fun loadDefaultWorldGraph(meshShapes: MeshShapeMap) =
    loadWorldGraph(meshShapes, mainScene())

fun newEditorResourceInfo(client: Client): ResourceInfo {
  return client.resourceInfo.copy(
      textures = client.resourceInfo.textures + reflectProperties<String>(PlaceholderTextures)
          .associateWith { defaultTextureAttributes }
  )
}

fun gatherCustomEditorCommands(editor: Editor, previousEditor: Editor): Commands {
  val graph = getCachedGraph(editor)
  val previousGraph = getActiveEditorGraph(previousEditor)
  return if (previousGraph == null)
    listOf()
  else
    updateSideNodeNames(editor, graph, previousGraph)
}

fun updateMarlothEditor(deviceStates: List<InputDeviceState>, world: World?, editor: Editor): Pair<Editor, Commands> {
  val editorCommands = prepareEditorUpdate(deviceStates, editor)
  val editorEvents = getEditorEvents(editor)(editorCommands, listOf())
  val editorWithWorld = updateEditorSyncing(world, editor)
  val nextEditor = updateEditor(deviceStates, editorCommands, editorWithWorld)
  val additionalCommands = gatherCustomEditorCommands(nextEditor, editor)
  val finalEditor = if (additionalCommands.any()) {
    val (nextState, nextHistory) = updateGraphStateAndHistory(nextEditor, additionalCommands)
    nextEditor.copy(
        persistentState = nextState,
        history = nextHistory,
    )
  } else
    nextEditor

  return finalEditor to editorEvents
}

enum class EditingMode {
  none,
  editor,
  aura,
}

fun newEditingMode(): EditingMode =
    when {
      getDebugBoolean("START_EDITOR") -> EditingMode.editor
      getDebugBoolean("START_AURA") -> EditingMode.aura
      else -> EditingMode.none
    }

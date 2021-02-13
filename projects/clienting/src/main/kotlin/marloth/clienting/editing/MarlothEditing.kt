package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.clienting.rendering.characterMeshes
import marloth.clienting.rendering.characterPlacement
import marloth.definition.data.characterDefinitions
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.*
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.*
import silentorb.mythic.editing.components.gizmoMenuToggleState
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.ExpansionLibrary
import silentorb.mythic.ent.scenery.expandInstances
import silentorb.mythic.ent.scenery.nodeAttributes
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.scenePropertiesSchema
import simulation.entities.DepictionType
import simulation.misc.Entities
import simulation.misc.GameAttributes
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

fun getMarlothEditorAttributes(): List<String> =
    commonEditorAttributes() + reflectProperties(GameAttributes)

fun marlothEditorPropertySchema() =
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

fun creatureDepiction(depictionType: DepictionType): EditorDepiction = { graph, node ->
  val initialTransform = getNodeTransform(graph, node)
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
            )
        )

fun newEditor(textLibrary: TextResourceMapper, meshShapes: MeshShapeMap): Editor {
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
          schema = marlothEditorPropertySchema(),
          textures = textures() + reflectProperties(PlaceholderTextures),
          attributes = getMarlothEditorAttributes(),
          meshes = reflectProperties(MeshId),
          meshShapes = meshShapes,
          collisionPresets = marlothCollisionPresets(),
          expanders = marlothExpanders(),
          depictions = marlothEditorDepictions(),
          menus = marlothEditorMenus(),
          gizmoPainters = listOf(blockBoundsPainter),
      ),
      fileItems = loadProjectTree(projectPath, "world"),
      persistentState = loadEditorStateOrDefault(),
      graphLibrary = newEditorGraphLibrary(textLibrary),
  )
}

fun updateEditingActive(commands: Commands, previousIsActive: Boolean): Boolean =
    when {
      commands.any { it.type == GuiCommandType.editor } -> !previousIsActive
      commands.any { it.type == GuiCommandType.newGame } -> false
      else -> previousIsActive
    }

fun filterOutEditorOnlyNodes(graph: Graph): Graph {
  val editorOnlyNodes = nodeAttributes(graph, CommonEditorAttributes.editorOnly)
  return graph
      .filter { !editorOnlyNodes.contains(it.source) }
      .toSet()
}

fun expandGameInstances(library: ExpansionLibrary, name: String): Graph =
    filterOutEditorOnlyNodes(expandInstances(library, name))

fun loadWorldGraph(meshShapes: MeshShapeMap, name: String): Graph {
  val graphLibrary = loadMarlothGraphLibrary(marlothPropertiesSerialization)
  assert(graphLibrary.contains(name))
  val library = ExpansionLibrary(graphLibrary, marlothExpanders(), marlothEditorPropertySchema(), meshShapes)
  return expandGameInstances(library, name)
}

const val defaultScene = "root"

fun mainScene() = getDebugString("DEFAULT_SCENE") ?: defaultScene

fun newExpansionLibrary(graphLibrary: GraphLibrary, meshShapes: Map<Key, Shape>) =
    ExpansionLibrary(graphLibrary, marlothExpanders(), marlothEditorPropertySchema(), meshShapes)

fun expandWorldGraph(editor: Editor, scene: String): Graph {
  val graphLibrary = loadAllDependencies(editor, scene)
  val library = newExpansionLibrary(graphLibrary, editor.enumerations.meshShapes)
  return expandGameInstances(library, scene)
}

fun expandDefaultWorldGraph(editor: Editor): Graph =
    expandWorldGraph(editor, mainScene())

fun loadDefaultWorldGraph(meshShapes: MeshShapeMap) =
    loadWorldGraph(meshShapes, mainScene())

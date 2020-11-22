package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.expandInstances
import silentorb.mythic.happenings.Commands
import silentorb.mythic.resource_loading.getUrlPath
import simulation.misc.GameAttributes
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

fun marlothCollisionPresets(): Map<Int, String> =
    reflectPropertiesMap<Int>(CollisionGroups)
        .map { it.value to it.key }
        .associate { it }

fun newEditor(): Editor {
  val debugProjectPath = getDebugString("EDITOR_PROJECT_PATH")
  val projectPath = if (debugProjectPath != null)
    Path.of(debugProjectPath)
  else
    getUrlPath(worldResourcePath)

  return Editor(
      projectPath = projectPath,
      enumerations = EditorEnumerations(
          propertyDefinitions = commonPropertyDefinitions(),
          textures = reflectProperties(TextureId),
          attributes = getMarlothEditorAttributes(),
          meshes = reflectProperties(MeshId),
          collisionPresets = marlothCollisionPresets()
      ),
      fileItems = loadProjectTree(projectPath, "world"),
      state = loadEditorStateOrDefault(),
  )
}

fun updateEditingActive(commands: Commands, previousIsActive: Boolean): Boolean =
    when {
      commands.any { it.type == GuiCommandType.editor } -> !previousIsActive
      commands.any { it.type == GuiCommandType.newGame } -> false
      else -> previousIsActive
    }

fun filterOutEditorOnlyNodes(graph: Graph): Graph {
  val editorOnlyNodes = filterByAttribute(graph, CommonEditorAttributes.editorOnly)
  return graph
      .filter { !editorOnlyNodes.contains(it.source) }
      .toSet()
}

fun expandGameInstances(graphs: GraphLibrary, name: String): Graph =
    filterOutEditorOnlyNodes(expandInstances(graphs, name))

fun loadWorldGraph(name: String): Graph {
  val graphLibrary = loadMarlothGraphLibrary(commonPropertyDefinitions())
  assert(graphLibrary.contains(name))
  return expandGameInstances(graphLibrary, name)
}

const val defaultWorldScene = "root"

fun expandWorldGraph(editor: Editor, scene: String): Graph {
  val graphLibrary = loadAllDependencies(editor, scene)
  return expandGameInstances(graphLibrary, scene)
}

fun expandDefaultWorldGraph(editor: Editor): Graph =
    expandWorldGraph(editor, defaultWorldScene)

fun loadDefaultWorldGraph() =
    loadWorldGraph(defaultWorldScene)

package marloth.clienting.editing

import marloth.clienting.input.GuiCommandType
import marloth.definition.data.characterDefinitions
import marloth.definition.misc.loadMarlothGraphLibrary
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.MeshShapeMap
import marloth.scenery.enums.TextResourceMapper
import marloth.scenery.enums.textures
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.*
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.ExpansionLibrary
import silentorb.mythic.ent.scenery.expandInstances
import silentorb.mythic.ent.scenery.filterByAttribute
import silentorb.mythic.happenings.Commands
import silentorb.mythic.resource_loading.getUrlPath
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.scenePropertiesSchema
import simulation.misc.Entities
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
      ) + editorLabel(key, key)
    }

fun newEditorGraphLibrary(textLibrary: TextResourceMapper): GraphLibrary =
    characterDefinitions()
        .mapValues { (key, definition) ->
          setOf(
              Entry(key, "", ""),
          ) + editorLabel(key, textLibrary(definition.name))
        } + spawners()

fun newEditor(textLibrary: TextResourceMapper, meshShapes: MeshShapeMap): Editor {
  val debugProjectPath = getDebugString("EDITOR_PROJECT_PATH")
  val projectPath = if (debugProjectPath != null)
    Path.of(debugProjectPath)
  else
    getUrlPath(worldResourcePath)

  return Editor(
      projectPath = projectPath,
      enumerations = EditorEnumerations(
          propertyDefinitions = commonPropertyDefinitions(),
          schema = scenePropertiesSchema(),
          textures = textures(),
          attributes = getMarlothEditorAttributes(),
          meshes = reflectProperties(MeshId),
          meshShapes = meshShapes,
          collisionPresets = marlothCollisionPresets(),
          expanders = marlothExpanders(),
      ),
      fileItems = loadProjectTree(projectPath, "world"),
      state = loadEditorStateOrDefault(),
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
  val editorOnlyNodes = filterByAttribute(graph, CommonEditorAttributes.editorOnly)
  return graph
      .filter { !editorOnlyNodes.contains(it.source) }
      .toSet()
}

fun expandGameInstances(library: ExpansionLibrary, name: String): Graph =
    filterOutEditorOnlyNodes(expandInstances(library, name))

fun loadWorldGraph(meshShapes: MeshShapeMap, name: String): Graph {
  val graphLibrary = loadMarlothGraphLibrary(commonPropertyDefinitions())
  assert(graphLibrary.contains(name))
  val library = ExpansionLibrary(graphLibrary, marlothExpanders(), scenePropertiesSchema(), meshShapes)
  return expandGameInstances(library, name)
}

const val defaultWorldScene = "root"

fun expandWorldGraph(editor: Editor, scene: String): Graph {
  val graphLibrary = loadAllDependencies(editor, scene)
  val library = ExpansionLibrary(graphLibrary, marlothExpanders(), scenePropertiesSchema(), editor.enumerations.meshShapes)
  return expandGameInstances(library, scene)
}

fun expandDefaultWorldGraph(editor: Editor): Graph =
    expandWorldGraph(editor, defaultWorldScene)

fun loadDefaultWorldGraph(meshShapes: MeshShapeMap) =
    loadWorldGraph(meshShapes, defaultWorldScene)

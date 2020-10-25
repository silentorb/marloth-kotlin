package marloth.integration.scenery

import marloth.clienting.rendering.GameScene
import silentorb.mythic.editing.*
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.MeshElement
import silentorb.mythic.lookinglass.ModelMeshMap
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.scenery.Scene
import simulation.misc.Definitions

fun nodeToElement(graph: Graph, meshEntries: Map<Id, String>, node: Id): ElementGroup? {
  val mesh = meshEntries[node]
  return if (mesh == null)
    null
  else {
    ElementGroup(
        meshes = listOf(
            MeshElement(
                mesh = mesh
            )
        )
    )
  }
}

fun sceneFromEditorGraph(meshes: ModelMeshMap, definitions: Definitions, editor: Editor): GameScene {
  val camera = newFlyThroughCamera { defaultFlyThroughState() }
  val graph = getActiveEditorGraph(editor) ?: listOf()
  val tree = getSceneTree(graph)
  val nodes = getTripleKeys(graph)
      .plus(tree.values)

  val meshEntries = groupProperty<String>(Properties.mesh)(graph)
  val layers = listOf(
      SceneLayer(
          elements = nodes.mapNotNull { node -> nodeToElement(graph,meshEntries, node) },
          useDepth = false
      ),
  )
  return GameScene(
      main = Scene(
          camera = camera,
          lights = listOf(),
          lightingConfig = defaultLightingConfig()
      ),
      layers = layers,
      filters = listOf()
  )
}

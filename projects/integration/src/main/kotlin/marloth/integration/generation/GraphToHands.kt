package marloth.integration.generation

import marloth.definition.data.newInteraction
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.nodeHasAttribute
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.getNodeCollisionObject
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import simulation.accessorize.Accessory
import simulation.accessorize.AccessoryStack
import simulation.entities.Depiction
import simulation.entities.Interactable
import simulation.main.NewHand
import simulation.misc.GameAttributes
import simulation.misc.MarlothProperties

fun getNodeBody(graph: Graph, node: Key, parentTransform: Matrix): Body? {
  val transform = parentTransform * getNodeTransform(graph, node)
  return if (transform == Matrix.identity)
    null
  else {
    Body(
        position = transform.translation(),
        orientation = Quaternion().fromUnnormalized(transform),
        scale = transform.getScale()
    )
  }
}

fun getNodeDepiction(graph: Graph, node: Key): Depiction? {
  val mesh = getNodeValue<String>(graph, node, SceneProperties.mesh)
  return if (mesh == null)
    null
  else {
    Depiction(
        mesh = mesh,
        texture = getNodeValue<String>(graph, node, SceneProperties.texture),
    )
  }
}

fun getNodeInteractions(graph: Graph, node: Key): List<Interactable> =
    getGraphValues<String>(graph, node, MarlothProperties.interaction)
        .map { type ->
          newInteraction(type)
        }

fun getNodeItemType(graph: Graph, node: Key): AccessoryStack? {
  val itemType = getNodeValue<String>(graph, node, MarlothProperties.itemType)
  return if (itemType != null)
    AccessoryStack(
        value = Accessory(
            type = itemType,
        )
    )
  else
    null
}

fun graphToHands(meshShapes: Map<String, Shape>, nextId: IdSource, graph: Graph, transform: Matrix): List<NewHand> {
  val keys = getGraphKeys(graph)
  return keys
      .mapNotNull { node ->
        val components = listOfNotNull(
            getNodeBody(graph, node, transform),
            getNodeDepiction(graph, node),
            getNodeCollisionObject(meshShapes, graph, node),
            getNodeItemType(graph, node),
        ) + getNodeInteractions(graph, node)
        if (components.any())
          NewHand(
              id = nextId(),
              components = components,
          )
        else
          null
      }
}

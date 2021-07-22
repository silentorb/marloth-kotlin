package marloth.integration.generation

import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.*
import silentorb.mythic.lookinglass.ResourceInfo
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.getNodeCollisionObject
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Quaternion
import simulation.accessorize.Accessory
import simulation.entities.Depiction
import simulation.entities.Interactable
import simulation.entities.PrimaryMode
import simulation.main.NewHand
import simulation.misc.GameProperties

fun bodyFromTransform(transform: Matrix) =
    Body(
        position = transform.translation(),
        orientation = Quaternion().fromUnnormalized(transform),
        scale = transform.getScale(),
    )

fun getNodeBody(transform: Matrix): Body? {
  return if (transform == Matrix.identity)
    null
  else
    bodyFromTransform(transform).copy(
        isKinetic = true,
    )
}

fun getNodeDepiction(resourceInfo: ResourceInfo, graph: Graph, node: Key): Depiction? {
  val mesh = getNodeValue<String>(graph, node, SceneProperties.mesh)
  return if (mesh == null)
    null
  else {
    Depiction(
        mesh = mesh,
        material = getNodeMaterial(resourceInfo, graph, node),
    )
  }
}

fun getPrimaryMode(graph: Graph, node: Key): PrimaryMode? {
  val modeType = getNodeValue<String>(graph, node, GameProperties.modeType)
  val mode = getNodeValue<String>(graph, node, GameProperties.mode)
  return if (modeType != null && mode != null)
    PrimaryMode(
        type = modeType,
        mode = mode,
    )
  else
    null
}

//fun getDynamicBody(graph: Graph, node: Key): DynamicBody? {
//  val mass = getNodeValue<Float>(graph, node, MarlothProperties.mass)
//  val resistance = getNodeValue<Float>(graph, node, MarlothProperties.mass)
//  val mass = getNodeValue<Float>(graph, node, MarlothProperties.mass)
//  return if (mass != null)
//    DynamicBody(
//        mass = mass,
//        gravity = false,
//        resistance =
//    )
//  else
//    null
//}

fun getNodeInteractions(graph: Graph, node: Key): List<Any> =
    getNodeValues<String>(graph, node, GameProperties.interaction)
        .map { type ->
          Interactable(type = type)
        }

fun getNodeItemType(graph: Graph, node: Key): Accessory? {
  val itemType = getNodeValue<String>(graph, node, GameProperties.itemType)
  return if (itemType != null)
    Accessory(
        type = itemType,
    )
  else
    null
}

fun associateHandParentBodies(graph: Graph, hands: Map<String, NewHand>) =
    hands.map { (node, hand) ->
      val parent = getNodeValue<Key>(graph, node, SceneProperties.parent)
      val parentHand = hands[parent]
      val parentId = parentHand?.id
      val parentBody = parentHand?.components?.filterIsInstance<Body>()?.firstOrNull()
      val body = hand.components.filterIsInstance<Body>().firstOrNull()

      if (parentBody != null && parentId != null && body != null) {
        hand.replaceComponent(
            body.copy(
                parent = parentId,
                localTransform = getLocalNodeTransform(graph, node)
            )
        )
      } else
        hand
    }

fun graphToHands(resourceInfo: ResourceInfo, nextId: IdSource, graph: Graph, keys: Collection<String>,
                 parentTransform: Matrix): List<NewHand> {
  val localTransforms = getLocalTransforms(graph)
  val hands = keys
      .mapNotNull { node ->
        val transform = parentTransform * getAbsoluteNodeTransform(graph, localTransforms, node)
        val light = getNodeLight(graph, node, transform)
        val components = if (light != null)
          listOf(light)
        else
          listOfNotNull(
              getNodeBody(transform),
              getNodeDepiction(resourceInfo, graph, node),
              getNodeCollisionObject(resourceInfo.meshShapes, graph, node),
              getNodeItemType(graph, node),
              getPrimaryMode(graph, node),
          ) + getNodeInteractions(graph, node)

        if (components.any())
          node to NewHand(
              id = nextId(),
              components = components,
          )
        else
          null
      }
      .associate { it }

  return associateHandParentBodies(graph, hands)
}

fun graphToHands(resourceInfo: ResourceInfo, nextId: IdSource, graph: Graph, transform: Matrix): List<NewHand> =
    graphToHands(resourceInfo, nextId, graph, getGraphKeys(graph), transform)

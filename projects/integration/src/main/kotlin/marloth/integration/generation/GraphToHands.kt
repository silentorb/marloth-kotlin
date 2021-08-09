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
import simulation.accessorize.IntrinsicReplenishment
import simulation.accessorize.replenishmentKey
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Interactable
import simulation.entities.PrimaryMode
import simulation.main.NewHand
import simulation.main.getComponent
import simulation.misc.GameAttributes
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
  val depiction = getNodeValue<String>(graph, node, GameProperties.depiction)
  return if (mesh == null || depiction == DepictionType.none)
    null
  else {
    Depiction(
        mesh = mesh,
        material = getNodeMaterial(resourceInfo, graph, node),
        type = depiction ?: DepictionType.staticMesh
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

fun getNodeInteractions(graph: Graph, node: Key): Interactable? {
  val action = getNodeValue<String>(graph, node, GameProperties.interaction)
  return if (action != null) {
    val onInteract = getNodeValue<String>(graph, node, GameProperties.onInteract)
    if (onInteract != null)
      Interactable(
          action = action,
          onInteract = onInteract,
      )
    else
      null
  } else
    null
}

fun getNodeAccessory(graph: Graph, node: Key): Accessory? {
  val itemType = getNodeValue<String>(graph, node, GameProperties.itemType)
  val removeOnEmpty = getNodeValue<Boolean>(graph, node, GameProperties.removeOnEmpty) ?: true
  val maxQuantity = getNodeValue<Int>(graph, node, GameProperties.maxQuantity) ?: 0
  val components = if (nodeHasAttribute(graph, node, GameAttributes.intrinsicReplenishment))
    mapOf(
        replenishmentKey to IntrinsicReplenishment(),
    )
  else
    mapOf()

  return if (itemType != null)
    Accessory(
        type = itemType,
        removeOnEmpty = removeOnEmpty,
        maxQuantity = maxQuantity,
        components = components,
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

      if (parentBody != null && parentId != null) {
        val replacements = listOfNotNull(
            getComponent<Body>(hand)?.copy(
                parent = parentId,
                localTransform = getLocalNodeTransform(graph, node)
            ),
            getComponent<Accessory>(hand)?.copy(
                owner = parentId,
            ),
        )

        if (replacements.any())
          hand.replaceComponents(replacements)
        else
          hand
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
              getNodeAccessory(graph, node),
              getPrimaryMode(graph, node),
              getNodeInteractions(graph, node),
          )

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

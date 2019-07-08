package generation

import generation.structure.wallHeight
import marloth.definition.ItemId
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import marloth.definition.templates.newDamageCloud
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Vector3
import simulation.physics.voidNode
import randomly.Dice
import marloth.definition.creatures
import simulation.main.*
import simulation.misc.*

data class CharacterTemplate(
    val faction: Id,
    val definition: CharacterDefinition
)

fun placeCharacter(realm: Realm, template: CharacterTemplate, nextId: IdSource, node: Id, position: Vector3): IdHand {
//  val node = dice.getItem(realm.locationNodes.drop(1))// Skip the node where the player starts
//  val wall = dice.getItem(node.walls)
//  val position = getVector3Center(node.position, realm.mesh.faces[wall]!!.edges[0].first)
  val id = nextId()
  return newCharacter(
      id = id,
      nextId = nextId,
      faction = template.faction,
      definition = template.definition,
      position = position,
      node = node,
      spirit = Spirit(
          pursuit = Pursuit()
      )
  )
}

fun placeCharacters(realm: Realm, dice: Dice, scale: Float): (IdSource) -> List<IdHand> {
  return { nextId ->
    //  val enemyCount = (10f * scale).toInt()
    //  val counts = listOf(2, 2)
//  val counts = listOf(8, 0)
    val counts = listOf(0, 8)
    val total = counts.sum()

    val walls = realm.locationNodes
        .drop(1) // Skip the node where the player starts
        .flatMap { node -> node.walls.map { Pair(node.id, it) } }

    val positions = dice.take(walls, total)
        .map { Pair(it.first, realm.nodeTable[it.first]!!.position) }

    val templates = listOf(
        CharacterTemplate(
            faction = 1,
            definition = creatures.ally
        ),
        CharacterTemplate(
            faction = 2,
            definition = creatures.monster
        )
    )

    val seeds = counts.mapIndexed { index, i -> (1..i).map { templates[index] } }
        .flatten()

    seeds.zip(positions) { seed, (node, position) ->
      placeCharacter(realm, seed, nextId, node, position)
    }
  }
}

//fun newDoor(realm: Realm, nextId: IdSource): (Id) -> Hand = { nodeId ->
//  val node = realm.nodeTable[nodeId]!!
//  val j = node.walls.map { realm.faces[it]!! }
//  val face = node.walls.first { realm.faces[it]!!.faceType != FaceType.wall }
//  val meshFace = realm.mesh.faces[face]!!
//  val edge = getFloor(meshFace)
//  val margin = 0.9f
//  val length = edge.first.distance(edge.second) * margin
//  val position = getCenter(meshFace.vertices)
//  val id = nextId()
//  val sideEdge = realm.mesh.faces[face]!!.edges.filter(isVerticalEdgeLimited).first()
//  val height = Math.abs(sideEdge.first.z - sideEdge.second.z)
//  EntityTemplates.door.copy(
//      id = id,
//      body = Body(
//          id = id,
//          position = position,
//          orientation = Quaternion().rotateTo(Vector3(0f, 1f, 0f), realm.mesh.faces[face]!!.normal),
//          scale = Vector3(length, 1f, height * margin),
//          node = nodeId
//      )
//  )
//}

//fun placeDoors(realm: Realm, nextId: IdSource): Deck =
//    toDeck(
//        realm.doorFrameNodes.map(newDoor(realm, nextId))
//    )

val isValidLampWall = { info: ConnectionFace ->
  info.faceType == FaceType.wall && info.texture != null
}

//fun placeWallLamps(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
//
//  val options = realm.locationNodes
//      .filter { node -> !realm.doorFrameNodes.contains(node.id) }
//      .filter { node ->
//        val infos = node.walls.map { realm.faces[it]!! }
//        infos.any(isValidLampWall)
//      }
//  if (options.none())
//    return Deck()
//
//  val count = Math.min((10f * scale).toInt(), options.size)
//  val nodes = dice.take(options, count)
//  val hands = nodes.mapNotNull { node ->
//    val options2 = node.walls.filter { isValidLampWall(realm.faces[it]!!) }
//    if (options2.any()) {
//      throw Error("Not implemented")
////      val wall = realm.mesh.faces[dice.getItem(options2)]!!
////      val edge = wall.edges[0]
////      val position = getVector3Center(edge.first, edge.second) +
////          Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
////      val angle = Quaternion().rotateTo(Vector3(1f, 0f, 0f), wall.normal)
////      val id = nextId()
////      EntityTemplates.wallLamp.copy(
////          id = id,
////          body = Body(
////              id = id,
////              position = position,
////              orientation = angle,
////              velocity = Vector3(),
////              node = node.id
////          )
////      )
//    } else
//      null
//  }
//
//  return toDeck(hands)
//}

fun newPlayer(nextId: IdSource, playerNode: Node): Deck {
  val characterId = nextId()

  val characterHand = newCharacter(
      id = characterId,
      nextId = nextId,
      faction = 1,
      definition = creatures.player,
      position = playerNode.position + Vector3(0f, 0f, 10f),
      node = playerNode.id,
      player = Player(
          playerId = 1,
          name = "Unknown Hero",
          viewMode = ViewMode.firstPerson
      )
  )

  val candleId = nextId()

  val candle = Hand(
      attachment = Attachment(
          target = characterId,
          category = AttachmentTypeId.item.name,
          index = 2
      ),
      entity = Entity(
          type = ItemId.candle.name
      )
  )

  val result = toDeck(characterHand.copy(hand = characterHand.hand.copy(character = characterHand.hand.character!! equip candleId)))
      .plus(toDeck(IdHand(
          id = candleId,
          hand = candle
      )))

  return result
}

fun addVoidNode(realm: Realm): Realm =
    realm.copy(
        nodeList = realm.nodeList.plus(voidNode)
    )

fun finalizeRealm(input: WorldInput, realm: Realm): World {
  val playerNode = realm.nodeTable.values.first { it.biome == BiomeId.home }
  val scale = calculateWorldScale(input.boundary.dimensions)
  val nextId = newIdSource(1)
  val particleNode = realm.nodeTable[6L]!!
  val baseColor = Vector3(0.5f, 1f, 0.5f)
  val deck = Deck()
      .plus(newPlayer(nextId, playerNode))
      .plus(allHandsOnDeck(listOf(
          newDamageCloud(
              position = particleNode.position + Vector3(0f, 0f, -wallHeight / 2f),
              radius = particleNode.radius
          )
      ), nextId))
//      .plus(placeWallLamps(realm, nextId, input.dice, scale))
//      .plus(placeDoors(realm, nextId))

  return World(
      deck = deck,
      nextId = nextId(),
      realm = realm,
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0
  )
}

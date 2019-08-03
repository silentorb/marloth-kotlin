package generation

import generation.abstracted.distributeToSlots
import generation.abstracted.normalizeRanges
import generation.architecture.alignWithNodeFloor
import generation.architecture.nodeFloorCenter
import generation.misc.BiomeId
import marloth.definition.creatures
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newBuffCloud
import marloth.definition.templates.newMerchant
import marloth.definition.templates.newTreasureChest
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Vector3
import randomly.Dice
import scenery.enums.AccessoryId
import scenery.enums.MeshId
import scenery.enums.ModifierId
import simulation.entities.*
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.main.*
import simulation.misc.*
import simulation.physics.Body
import simulation.physics.CollisionObject

data class CharacterTemplate(
    val faction: Id,
    val definition: CharacterDefinition
)

fun placeCharacter(realm: Realm, template: CharacterTemplate, nextId: IdSource, node: Id, position: Vector3): IdHand {
//  val node = dice.takeOne(realm.locationNodes.drop(1))// Skip the node where the player starts
//  val wall = dice.takeOne(node.walls)
//  val position = getVector3Center(node.position, realm.mesh.faces[wall]!!.edges[0].first)
  val id = nextId()
  return IdHand(id, newCharacter(
      nextId = nextId,
      faction = template.faction,
      definition = template.definition,
      position = position,
      spirit = Spirit(
          pursuit = Pursuit()
      )
  ))
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
////      val wall = realm.mesh.faces[dice.takeOne(options2)]!!
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

  val characterHand = IdHand(characterId, newCharacter(
      nextId = nextId,
      faction = 1,
      definition = creatures.player,
      position = playerNode.position + Vector3(0f, 0f, 10f),
      player = Player(
          playerId = 1,
          name = "Unknown Hero",
          viewMode = ViewMode.firstPerson
      )
  ))

  val candleId = nextId()

  val candle = Hand(
      attachment = Attachment(
          target = characterId,
          category = AttachmentTypeId.equipped,
          index = 2
      ),
      accessory = Accessory(
          type = AccessoryId.candle
      )
  )

  val result = toDeck(characterHand.copy(hand = characterHand.hand.copy(character = characterHand.hand.character!! equip candleId)))
      .plus(toDeck(IdHand(
          id = candleId,
          hand = candle
      )))

  return result
}

fun placeBuffCloud(node: Node, buff: ModifierId) =
    newBuffCloud(
        position = nodeFloorCenter(node),
        radius = node.radius,
        buff = buff
    )

fun placeTreasureChest(meshInfo: MeshInfoMap, node: Node, amount: Int) =
    newTreasureChest(meshInfo, alignWithNodeFloor(meshInfo, node, MeshId.treasureChest), amount)

enum class Occupant {
  coldCloud,
  fireCloud,
  merchant,
  none,
  poisonCloud,
  treasureChest
}

typealias DistributionMap = Map<Occupant, Int>

fun occupantPopulators(meshInfo: MeshInfoMap, node: Node, nextId: IdSource, occupant: Occupant): Hand =
    when (occupant) {
      Occupant.coldCloud -> placeBuffCloud(node, ModifierId.damageChilled)
      Occupant.fireCloud -> placeBuffCloud(node, ModifierId.damageBurning)
      Occupant.merchant -> newMerchant(nextId, node.position, defaultWares)
      Occupant.none -> Hand()
      Occupant.poisonCloud -> placeBuffCloud(node, ModifierId.damagePoisoned)
      Occupant.treasureChest -> placeTreasureChest(meshInfo, node, 10)
    }

fun damageCloudsDistributions(dice: Dice, totalWeight: Int): DistributionMap {
  val cloudTypes = listOf(
      Occupant.coldCloud,
      Occupant.fireCloud,
      Occupant.poisonCloud
  )

  val initialWeights = cloudTypes
      .map { Pair(it, dice.getInt(0, 100)) }
      .associate { it }

  return normalizeRanges(totalWeight, initialWeights)
}

fun getDistributions(dice: Dice): DistributionMap = mapOf(
//    Occupant.merchant to 200,
    Occupant.none to 300,
    Occupant.treasureChest to 600
).plus(damageCloudsDistributions(dice, 500))

fun populateRooms(meshInfo: MeshInfoMap, dice: Dice, nextId: IdSource, realm: Realm, playerNode: Id): Deck {
  val rooms = getRooms(realm).filter { it.id != playerNode }
  val ranges = getDistributions(dice)
  val hands = distributeToSlots(dice, rooms.size, ranges)
      .zip(rooms) { occupant, node ->
        occupantPopulators(meshInfo, node, nextId, occupant)
      }

  return Deck()
      .plus(allHandsOnDeck(hands, nextId))
}

fun populateWorld(meshInfo: MeshInfoMap, input: WorldInput): WorldTransform = { world ->
  val playerNode = world.realm.nodeTable.values.first { it.biome == BiomeId.home }
  val scale = calculateWorldScale(input.boundary.dimensions)
  val nextId = newIdSource(1)
  val deck = Deck()
      .plus(newPlayer(nextId, playerNode))
      .plus(populateRooms(meshInfo, input.dice, nextId, world.realm, playerNode.id))
//      .plus(placeWallLamps(realm, nextId, input.dice, scale))
//      .plus(placeDoors(realm, nextId))

  world.copy(
      deck = deck,
      nextId = nextId()
  )
}

fun finalizeRealm(realm: Realm): World {
  val nextId = newIdSource(1)
  return World(
      deck = Deck(),
      nextId = nextId(),
      realm = realm,
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0
  )
}

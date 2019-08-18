package generation

import generation.abstracted.distributeToSlots
import generation.abstracted.neighbors
import generation.abstracted.normalizeRanges
import generation.architecture.alignWithNodeFloor
import generation.architecture.nodeFloorCenter
import generation.misc.BiomeId
import marloth.definition.EntityTemplates
import marloth.definition.creatures
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newBuffCloud
import marloth.definition.templates.newMerchant
import marloth.definition.templates.newTreasureChest
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.times
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
import simulation.physics.old.getLookAtAngle

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
      definition = template.definition,
      faction = template.faction,
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

//val isValidLampWall = { info: ConnectionFace ->
//  info.faceType == FaceType.wall && info.texture != null
//}

fun gatherNodeWallMap(deck: Deck): Map<Id, Set<Id>> =
    deck.architecture.entries
        .filter { it.value.isWall }
        .groupBy { (id, _) ->
          val body = deck.bodies[id]!!
          body.nearestNode
        }
        .mapValues { it.value.map { i -> i.key }.toSet() }

fun placeWallLamps(deck: Deck, realm: Realm, dice: Dice, scale: Float): List<Hand> {
  val nodeWalls = gatherNodeWallMap(deck)
  if (nodeWalls.none())
    return listOf()

  val count = Math.min((10f * scale).toInt(), nodeWalls.size)
  val nodes = dice.take(nodeWalls.entries, count)
  val hands = nodes.mapNotNull { (node, options) ->
    if (options.any()) {
      val wallId = dice.takeOne(options)
      val wallBody = deck.bodies[wallId]!!
      val wallShape = deck.collisionShapes[wallId]
      val position = wallBody.position +
          Vector3(0f, 0f, 0.1f) + wallBody.orientation * Vector3(-0.5f, 0f, 0f)
      val orientation = Quaternion(wallBody.orientation).rotateZ(Pi)
      EntityTemplates.wallLamp.copy(
          body = Body(
              position = position,
              orientation = orientation,
              velocity = Vector3(),
              nearestNode = node
          )
      )
    } else
      null
  }

  return hands
}

fun newPlayer(realm: Realm, playerNode: Node): Hand {
  val neighbor = neighbors(realm.graph, playerNode).first()
  return newCharacter(
      definition = creatures.player,
      faction = 1,
      position = playerNode.position + Vector3(0f, 0f, 1f),
      angle = getLookAtAngle(neighbor.position - playerNode.position)
  )
      .copy(
          attachments = listOf(
              HandAttachment(
                  category = AttachmentTypeId.equipped,
                  index = 2,
                  hand = Hand(
                      accessory = Accessory(
                          type = AccessoryId.candle
                      )
                  )
              )
          ),
          player = Player(
              playerId = 1,
              name = "Unknown Hero",
              viewMode = ViewMode.firstPerson
          )
      )

//  val candle = Hand(
//      attachment = Attachment(
//          category = AttachmentTypeId.equipped,
//          index = 2
//      ),
//      accessory = Accessory(
//          type = AccessoryId.candle
//      )
//  )

//  val result = toDeck(characterHand.copy(hand = characterHand.hand.copy(character = characterHand.hand.character!! equip candleId)))
//      .plus(toDeck(IdHand(
//          id = candleId,
//          hand = candle
//      )))
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

fun occupantPopulators(meshInfo: MeshInfoMap, node: Node, occupant: Occupant): Hand =
    when (occupant) {
      Occupant.coldCloud -> placeBuffCloud(node, ModifierId.damageChilled)
      Occupant.fireCloud -> placeBuffCloud(node, ModifierId.damageBurning)
      Occupant.merchant -> newMerchant(node.position, defaultWares)
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

fun populateRooms(meshInfo: MeshInfoMap, dice: Dice, realm: Realm, playerNode: Id): List<Hand> {
  val rooms = getRooms(realm).filter { it.id != playerNode }
  val ranges = getDistributions(dice)
  val hands = distributeToSlots(dice, rooms.size, ranges)
      .zip(rooms) { occupant, node ->
        occupantPopulators(meshInfo, node, occupant)
      }

  return hands
}

fun populateWorld(meshInfo: MeshInfoMap, input: WorldInput, realm: Realm): (Deck) -> List<Hand> = { deck ->
  val playerNode = realm.nodeTable.values.first { it.biome == BiomeId.home }
  val scale = calculateWorldScale(input.boundary.dimensions)
  listOf(newPlayer(realm, playerNode))
      .plus(populateRooms(meshInfo, input.dice, realm, playerNode.id))
      .plus(placeWallLamps(deck, realm, input.dice, scale))
//      .plus(placeDoors(realm, nextId))
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

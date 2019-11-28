package marloth.generation

import generation.abstracted.distributeToSlots
import generation.abstracted.neighbors
import generation.abstracted.normalizeRanges
import generation.architecture.old.alignWithNodeFloor
import generation.architecture.building.floorOffset
import generation.architecture.old.nodeFloorCenter
import generation.architecture.misc.calculateWorldScale
import generation.architecture.misc.applyCellPosition
import generation.general.BiomeAttribute
import generation.architecture.misc.GenerationConfig
import generation.architecture.definition.MeshAttribute
import generation.architecture.misc.MeshInfoMap
import marloth.definition.creatures
import marloth.definition.newCharacter
import marloth.definition.templates.defaultWares
import marloth.definition.templates.newBuffCloud
import marloth.definition.templates.newMerchant
import marloth.definition.templates.newTreasureChest
import mythic.ent.Id
import mythic.ent.getDebugSetting
import mythic.spatial.*
import org.joml.times
import randomly.Dice
import scenery.enums.AccessoryId
import scenery.enums.MeshId
import scenery.enums.ModifierId
import simulation.entities.*
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.HandAttachment
import simulation.misc.*
import simulation.physics.Body
import simulation.physics.old.getLookAtAngle


fun placeAiCharacter(faction: Id, definition: CharacterDefinition, position: Vector3): Hand {
  return newCharacter(
      definition = definition,
      faction = faction,
      position = position,
      spirit = Spirit(
          pursuit = Pursuit()
      )
  )
}

fun placeEnemy(node: Node): Hand =
    placeAiCharacter(
        faction = monsterFaction,
        definition = creatures.monster,
        position = node.position
    )

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

fun gatherNodeWallMap(deck: Deck, filter: (Map.Entry<Id, ArchitectureElement>) -> Boolean): Map<Id, Set<Id>> =
    deck.architecture.entries
        .filter { it.value.isWall }
        .filter(filter)
        .groupBy { (id, _) ->
          val body = deck.bodies[id]!!
          body.nearestNode
        }
        .mapValues { it.value.map { i -> i.key }.toSet() }

fun placeWallLamps(deck: Deck, config: GenerationConfig, realm: Realm, dice: Dice, scale: Float): List<Hand> {
  val nodeWalls = gatherNodeWallMap(deck) {
    val depiction = deck.depictions[it.key]!!
    config.meshes[depiction.mesh!!]!!.attributes.contains(MeshAttribute.canHaveAttachment)
  }
  if (nodeWalls.none())
    return listOf()

  val (certain, options) = nodeWalls.entries.partition {
    val biome = config.biomes[realm.nodeTable[it.key]?.biome]
    biome != null && biome.attributes.contains(BiomeAttribute.alwaysLit)
  }

  val count = Math.min((10f * scale).toInt(), options.size)
  val nodes = dice.take(options, count)
      .plus(certain)
  val hands = nodes.mapNotNull { (node, options) ->
    if (options.any()) {
      val wallId = dice.takeOne(options)
      val wallBody = deck.bodies[wallId]!!
      val wallShape = deck.collisionShapes[wallId]
      val position = wallBody.position +
          Vector3(0f, 0f, 0.1f) + wallBody.orientation * Vector3(-0.5f, 0f, 0f)
      val orientation = Quaternion(wallBody.orientation).rotateZ(Pi)
      Hand(
          depiction = Depiction(
              type = DepictionType.staticMesh,
              mesh = MeshId.wallLamp.toString()
          ),
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

fun newPlayer(grid: MapGrid, cellPosition: Vector3i): Hand {
  val neighbor = cellNeighbors(grid.connections, cellPosition).first()
  return newCharacter(
      definition = creatures.player,
      faction = 1,
      position = applyCellPosition(cellPosition) + floorOffset + Vector3(0f, 0f, 6f),
      angle = getLookAtAngle((neighbor - cellPosition).toVector3())
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
              name = "Unknown Hero",
              viewMode = ViewMode.firstPerson
          )
      )
}

fun placeBuffCloud(node: Node, buff: ModifierId) =
    newBuffCloud(
        position = nodeFloorCenter(node),
        radius = node.radius,
        buff = buff
    )

fun placeTreasureChest(meshInfo: MeshInfoMap, node: Node, amount: Int) =
    newTreasureChest(meshInfo, alignWithNodeFloor(meshInfo, node, MeshId.treasureChest.name) + floorOffset, amount)

enum class Occupant {
  coldCloud,
  fireCloud,
  enemy,
  merchant,
  none,
  poisonCloud,
  treasureChest
}

typealias DistributionMap = Map<Occupant, Int>

typealias OccupantToHand = (Node, Occupant) -> Hand?

fun occupantPopulator(config: GenerationConfig): OccupantToHand = { node, occupant ->
  when (occupant) {
    Occupant.coldCloud -> placeBuffCloud(node, ModifierId.damageChilled)
    Occupant.fireCloud -> placeBuffCloud(node, ModifierId.damageBurning)
    Occupant.enemy -> if (config.includeEnemies) placeEnemy(node) else null
    Occupant.merchant -> newMerchant(node.position, defaultWares)
    Occupant.none -> null
    Occupant.poisonCloud -> placeBuffCloud(node, ModifierId.damagePoisoned)
    Occupant.treasureChest -> placeTreasureChest(config.meshes, node, 10)
  }
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

fun scalingDistributions(dice: Dice): DistributionMap = mapOf(
    Occupant.enemy to 0,
    Occupant.merchant to 0,
    Occupant.none to 30,
    Occupant.treasureChest to 20
).plus(damageCloudsDistributions(dice, 0))
//).plus(damageCloudsDistributions(dice, 10))

fun fixedDistributions(): DistributionMap = mapOf(
    Occupant.enemy to 1,
    Occupant.merchant to 0,
    Occupant.none to 0,
    Occupant.treasureChest to 0
)

fun populateRooms(occupantToHand: OccupantToHand, dice: Dice, realm: Realm, playerNode: Id): List<Hand> {
  if (System.getenv("NO_OBJECTS") != null)
    return listOf()

  val rooms = getRooms(realm).filter { it.id != playerNode && it.attributes.contains(NodeAttribute.fullFloor) }
  val scaling = scalingDistributions(dice)
  val fixed = fixedDistributions()
  val occupants = distributeToSlots(dice, rooms.size, scaling, fixed)
  val hands = rooms
      .zip(occupants, occupantToHand)
      .filterNotNull()

  return hands
}

fun getPlayerCell(grid: MapGrid) =
    grid.cells.entries.first { it.value.attributes.contains(NodeAttribute.home) }.key

fun populateWorld(config: GenerationConfig, input: WorldInput, realm: Realm): (Deck) -> List<Hand> = { deck ->
  val playerNodeOld = realm.nodeTable.values.firstOrNull {
    config.biomes[it.biome]!!.attributes.contains(BiomeAttribute.placeOnlyAtStart)
  }

  if (playerNodeOld == null)
    throw Error("Biome configuration is missing placeOnlyAtStart")

  val grid = realm.grid
  val playerCell = getPlayerCell(grid)
  val scale = calculateWorldScale(input.boundary.dimensions)
  val occupantToHand = occupantPopulator(config)
  val playerCount = getDebugSetting("INITIAL_PLAYER_COUNT")?.toInt() ?: 1
  (1..playerCount).map { newPlayer(grid, playerCell) }
      .plus(populateRooms(occupantToHand, input.dice, realm, playerNodeOld.id))
      .plus(placeWallLamps(deck, config, realm, input.dice, scale))
      .plus(listOf(
          Hand(
              cycle = Cycle(0.006f, 0f)
          ),
          Hand(
              cycle = Cycle(0.002f, 0.2f)
          )
      ))
}

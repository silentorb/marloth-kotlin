package generation

import generation.structure.wallHeight
import intellect.Pursuit
import intellect.Spirit
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import physics.Body
import physics.voidNode
import randomly.Dice
import scenery.Cylinder
import scenery.TextureId
import simulation.*
import simulation.data.creatures
import simulation.particles.Emitter
import simulation.particles.ParticleAppearance
import simulation.particles.ParticleEffect

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
          id = 0,
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
          id = 0,
          name = "Unknown Hero",
          viewMode = ViewMode.firstPerson
      )
  )

  val candleId = nextId()

  val candle = Item(
      id = candleId,
      owner = characterHand.hand.character!!.id,
      slot = 2,
      type = ItemType.candle
  )

  return toDeck(characterHand.copy(hand = characterHand.hand.copy(character = characterHand.hand.character!! equip candle.id)))
      .plus(toDeck(IdHand(
          id = candleId,
          hand = Hand(item = candle)
      )))
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
  val deck = Deck()
      .plus(newPlayer(nextId, playerNode))
      .plus(allHandsOnDeck(listOf(Hand(
          body = Body(
              position = particleNode.position + Vector3(0f, 0f, -wallHeight / 2f)
          ),
          particleEffect = ParticleEffect(
              lifecycle = listOf(
                  ParticleAppearance(
                      texture = TextureId.perlinParticle,
                      color = Vector4(0.5f, 1f, 0.5f, 1f),
                      size = 2f
                  )
              ),
              emitter = Emitter(
                  particlesPerSecond = 30f,
                  volume = Cylinder(radius = particleNode.radius, height = 10f),
                  life = Pair(1f, 2f),
                  initialVelocity = Vector3(0f, 0f, 1f)
              )
          )
      )), nextId))
//      .plus(placeWallLamps(realm, nextId, input.dice, scale))
//      .plus(placeDoors(realm, nextId))

  return World(
      deck = deck,
      nextId = nextId(),
      realm = addVoidNode(realm),
      dice = Dice()
  )
}

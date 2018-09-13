package generation

import colliding.Sphere
import intellect.Pursuit
import intellect.Spirit
import mythic.sculpting.FlexibleFace
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.getVector3Center
import physics.Body
import randomly.Dice
import simulation.*

fun placeEnemy(realm: Realm, nextId: IdSource, dice: Dice): Hand {
  val node = dice.getItem(realm.locationNodes.drop(1))// Skip the node where the player starts
  val wall = dice.getItem(node.walls)
  val position = getVector3Center(node.position, Vector3(wall.edges[0].first))
  return newCharacter(
      nextId = nextId,
      faction = 2,
      definition = characterDefinitions.monster,
      position = position,
      node = node,
      spirit = Spirit(
          id = 0,
          pursuit = Pursuit()
      )
  )
}

fun placeEnemies(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
//  val enemyCount = (10f * scale).toInt()
  val enemyCount = 1
  return toDeck((0 until enemyCount).map {
    placeEnemy(realm, nextId, dice)
  })
}

fun placeDoors(realm: Realm, nextId: IdSource): Deck =
    toDeck(realm.nodes.filter { it.biome == Biome.home }
        .flatMap { node ->
          node.walls.filter {
            val info = getFaceInfo(it)
            info.type == FaceType.space
                && getOtherNode(info, node)!!.biome != Biome.home
          }
        }
        .map { face ->
          val floor = getFloor(face)
          val id = nextId()
          Hand(
              door = Door(
                  id = id
              ),
              body = Body(
                  id = id,
                  position = Vector3(floor.middle),
                  orientation = Quaternion().rotateTo(Vector3(0f, 1f, 0f), face.normal),
                  attributes = doodadBodyAttributes,
                  shape = null,
                  gravity = false,
                  node = getFaceInfo(face).firstNode!!
              )
          )

        })


fun placeWallLamps(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
  val count = (10f * scale).toInt()
  val isValidLampWall = { it: FlexibleFace ->
    getFaceInfo(it).type == FaceType.wall && getFaceInfo(it).texture != null
  }
  val options = realm.locationNodes.filter { it.walls.any(isValidLampWall) }
  assert(options.any())
  val nodes = dice.getList(options, count)
  val hands = nodes.mapNotNull { node ->
    val options2 = node.walls.filter(isValidLampWall)
    if (options2.any()) {
      val wall = dice.getItem(options2)
      val edge = wall.edges[0]
      val position = getVector3Center(Vector3(edge.first), Vector3(edge.second)) +
          Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
//    val angle = getAngle(edge.first.copy().cross(edge.second).xy())
//    val angle = getAngle(wall.normal.xy())
      val angle = Quaternion().rotateTo(Vector3(1f, 0f, 0f), wall.normal)
//      instantiator.createWallLamp(position, node, angle)
      val id = nextId()
      Hand(
          body = Body(
              id = id,
              shape = Sphere(0.3f),
              position = position,
              orientation = angle,
              velocity = Vector3(),
              node = node,
              attributes = doodadBodyAttributes,
              gravity = false
          ),
          depiction = Depiction(
              id = id,
              type = DepictionType.wallLamp
          )
      )
    } else
      null
  }

  return toDeck(hands)
}

fun newPlayer(nextId: IdSource, playerNode: Node): Hand =
    newCharacter(
        nextId = nextId,
        faction = 1,
        definition = characterDefinitions.player,
        position = playerNode.position,
        node = playerNode,
        player = Player(
            playerId = 1,
            character = 0,
            viewMode = ViewMode.firstPerson
        )
    )


fun finalizeRealm(input: WorldInput, realm: Realm): World {
  val playerNode = realm.nodes.first()
  val scale = calculateWorldScale(input.boundary.dimensions)
  val nextId = newIdSource(1)
  val deck = Deck(
      factions = listOf(
          Faction(1, "Misfits"),
          Faction(2, "Monsters")
      )
  )
      .plus(toDeck(newPlayer(nextId, playerNode)))
      .plus(placeWallLamps(realm, nextId, input.dice, scale))
      .plus(placeDoors(realm, nextId))
//  instantiator.newPlayer(1)

//  placeWallLamps(world, instantiator, input.dice, scale)

  return World(
      deck = deck,
      nextId = nextId(),
      realm = realm)
}
package generation

import colliding.Sphere
import intellect.Pursuit
import intellect.Spirit
import mythic.ent.Id
import mythic.ent.IdSource
import mythic.ent.newIdSource
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.getCenter
import mythic.spatial.getVector3Center
import physics.Body
import randomly.Dice
import simulation.*

data class CharacterTemplate(
    val faction: Id,
    val definition: CharacterDefinition
)

fun placeCharacter(realm: Realm, template: CharacterTemplate, nextId: IdSource, node: Id, position: Vector3): Hand {
//  val node = dice.getItem(realm.locationNodes.drop(1))// Skip the node where the player starts
//  val wall = dice.getItem(node.walls)
//  val position = getVector3Center(node.position, realm.mesh.faces[wall]!!.edges[0].first)
  return newCharacter(
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

fun placeCharacters(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
//  val enemyCount = (10f * scale).toInt()
  val counts = listOf(2, 2)
  val total = counts.sum()

  val walls = realm.locationNodes
      .drop(1) // Skip the node where the player starts
      .flatMap { node -> node.walls.map { Pair(node.id, it) } }

  val positions = dice.take(walls, total)
      .map { Pair(it.first, getVector3Center(realm.nodeTable[it.first]!!.position, realm.mesh.faces[it.second]!!.edges[0].first)) }

  val templates = listOf(
      CharacterTemplate(
          faction = 1,
          definition = characterDefinitions.ally
      ),
      CharacterTemplate(
          faction = 2,
          definition = characterDefinitions.monster
      )
  )

  val seeds = counts.mapIndexed { index, i -> (1..i).map { templates[index] } }
      .flatten()

  return toDeck(seeds.zip(positions) { seed, (node, position) ->
    placeCharacter(realm, seed, nextId, node, position)
  })
}

fun placeDoors(realm: Realm, nextId: IdSource): Deck =
    toDeck(
        realm.doorFrameNodes.map { nodeId ->
          val node = realm.nodeTable[nodeId]!!
          val face = node.walls.first { realm.faces[it]!!.faceType != FaceType.wall }
          val id = nextId()
          Hand(
              door = Door(
                  id = id
              ),
              body = Body(
                  id = id,
                  position = getCenter(realm.mesh.faces[node.floors.first()]!!.vertices),
                  orientation = Quaternion().rotateTo(Vector3(0f, 1f, 0f), realm.mesh.faces[face]!!.normal),
                  attributes = doodadBodyAttributes,
                  shape = null,
                  gravity = false,
                  node = nodeId
              )
          )

        })

val isValidLampWall = { info: ConnectionFace ->
  info.faceType == FaceType.wall && info.texture != null
}

fun placeWallLamps(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {

  val options = realm.locationNodes
      .filter { node -> !realm.doorFrameNodes.contains(node.id) }
      .filter { node ->
        val infos = node.walls.map { realm.faces[it]!! }
        infos.any(isValidLampWall)
      }
  if (options.none())
    return Deck()

  val count = Math.min((10f * scale).toInt(), options.size)
  val nodes = dice.take(options, count)
  val hands = nodes.mapNotNull { node ->
    val options2 = node.walls.filter { isValidLampWall(realm.faces[it]!!) }
    if (options2.any()) {
      val wall = realm.mesh.faces[dice.getItem(options2)]!!
      val edge = wall.edges[0]
      val position = getVector3Center(edge.first, edge.second) +
          Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
      val angle = Quaternion().rotateTo(Vector3(1f, 0f, 0f), wall.normal)
      val id = nextId()
      Hand(
          body = Body(
              id = id,
              shape = Sphere(0.3f),
              position = position,
              orientation = angle,
              velocity = Vector3(),
              node = node.id,
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
        node = playerNode.id,
        player = Player(
            playerId = 1,
            character = 0,
            viewMode = ViewMode.firstPerson
        )
    )

fun finalizeRealm(input: WorldInput, realm: Realm): World {
  val playerNode = realm.nodeList.first()
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

  return World(
      deck = deck,
      nextId = nextId(),
      realm = realm
  )
}
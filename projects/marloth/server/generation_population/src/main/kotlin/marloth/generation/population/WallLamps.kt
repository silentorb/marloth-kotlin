package marloth.generation.population

import generation.architecture.definition.MeshAttribute
import generation.architecture.misc.GenerationConfig
import generation.general.BiomeAttribute
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.times
import silentorb.mythic.randomly.Dice
import marloth.scenery.enums.MeshId
import silentorb.mythic.ent.Id
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.main.Hand
import simulation.misc.Realm
import silentorb.mythic.physics.Body
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.Vector3i
import java.lang.Float.max

data class BoundingObject(
    val body: Body,
    val shape: Shape
)

fun toBoundingObjects(deck: Deck, ids: Collection<Id>): Map<Id, BoundingObject> =
    ids.associateWith { id ->
      BoundingObject(
          body = deck.bodies[id]!!,
          shape = deck.collisionShapes[id]!!.shape
      )
    }

fun approximatelyCollides(boundingCenter: Vector3, boundingRadius: Float): (BoundingObject) -> Boolean {
  val boundingCenterHorizontal = boundingCenter.xy()
  return { (body, shape) ->
    val horizontalMax = max(shape.x, shape.y) / 2f
    val height = body.position.z
    val halfHeight = shape.height / 2f
    val result = boundingCenterHorizontal.distance(body.position.xy()) <= boundingRadius + horizontalMax
        && height < boundingCenter.z + halfHeight
        && height > boundingCenter.z - halfHeight
    if (result) {
      val k = 0
    }
    result
  }
}

// Currently doesn't differentiate which side the of the wall is being blocked by other objects
fun wallHasRoomForLamp(boundingObjects: Map<Id, BoundingObject>, deck: Deck, wall: Id): Boolean {
  val wallBody = deck.bodies[wall]!!
  val result = boundingObjects
      .minus(wall)
      .values
      .none(approximatelyCollides(wallBody.position, 2f))
  return result
}

fun placeWallLamps(deck: Deck, config: GenerationConfig, realm: Realm,
                   dice: Dice, architectureCells: Map<Id, Vector3i>): List<Hand> {

  val boundingObjects = toBoundingObjects(deck, deck.bodies.keys)
  val attachmentWalls = deck.depictions.keys
      .filter { id ->
        val depiction = deck.depictions[id]!!
        val attributes = config.meshes[depiction.mesh!!]!!.attributes
        attributes.contains(MeshAttribute.wall)
            && attributes.contains(MeshAttribute.canHaveAttachment)
            && wallHasRoomForLamp(boundingObjects, deck, id)
      }

  val nodeWalls = attachmentWalls
      .groupBy { architectureCells[it]!! }

  if (nodeWalls.none())
    return listOf()

  val (certain, options) = nodeWalls.entries
      .partition { (position, _) ->
        val biome = config.biomes[realm.cellBiomes[position]!!]
        biome != null && biome.attributes.contains(BiomeAttribute.alwaysLit)
      }

//  val count = Math.min((10f * scale).toInt(), options.size)
  val count = (options.size.toFloat() * 0.8f).toInt()
  val nodes = dice.take(options, count)
      .plus(certain)
  val hands = nodes.mapNotNull { (_, options) ->
    if (options.any()) {
      val wallId = dice.takeOne(options)
      val wallBody = deck.bodies[wallId]!!
      val wallShape = deck.collisionShapes[wallId]
      val shape = wallShape!!.shape
      val heightOffset = dice.getFloat(2f, 3f) - shape.height / 2f
      val orientation = Quaternion(wallBody.orientation).rotateZ(Pi / 2f)
      val position = wallBody.position +
          Vector3(0f, 0f, heightOffset) + orientation * Vector3(shape.y / 2f, dice.getFloat(-1f, 1f), 0f)
      Hand(
          depiction = Depiction(
              type = DepictionType.staticMesh,
              mesh = MeshId.wallLamp.toString()
          ),
          body = Body(
              position = position,
              orientation = orientation,
              velocity = Vector3()
          )
      )
    } else
      null
  }

  return hands
}

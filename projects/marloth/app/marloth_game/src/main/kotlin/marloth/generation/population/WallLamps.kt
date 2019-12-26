package marloth.generation.population

import generation.architecture.definition.MeshAttribute
import generation.architecture.misc.GenerationConfig
import generation.general.BiomeAttribute
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import org.joml.times
import silentorb.mythic.randomly.Dice
import marloth.scenery.enums.MeshId
import silentorb.mythic.ent.Id
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.main.Hand
import simulation.misc.Realm
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Vector3i

fun placeWallLamps(deck: Deck, config: GenerationConfig, realm: Realm,
                   dice: Dice, scale: Float, architectureCells: Map<Id, Vector3i>): List<Hand> {
  val allWalls = deck.architecture.entries
      .filter { it.value.isWall }
      .map { it.key }

  val attachmentWalls = allWalls.filter {
    val depiction = deck.depictions[it]!!
    config.meshes[depiction.mesh!!]!!.attributes.contains(MeshAttribute.canHaveAttachment)
  }

  val nodeWalls = attachmentWalls
      .groupBy { architectureCells[it] }

  if (nodeWalls.none())
    return listOf()

  val (certain, options) = nodeWalls.entries.partition { (position, _) ->
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

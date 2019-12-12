package marloth.generation.population

import generation.architecture.definition.MeshAttribute
import generation.architecture.misc.GenerationConfig
import generation.general.BiomeAttribute
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.times
import randomly.Dice
import marloth.scenery.enums.MeshId
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Deck
import simulation.main.Hand
import simulation.misc.Realm
import simulation.physics.Body

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

package generation.architecture.building

import marloth.scenery.enums.MeshAttribute
import marloth.scenery.enums.MeshInfoMap
import generation.architecture.misc.MeshQuery
import generation.architecture.misc.meshMatches
import generation.general.oppositeDirections
import marloth.scenery.enums.MeshId
import silentorb.mythic.physics.Body
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Hand

data class WallLampInput(
    val wallPosition: Vector3,
    val orientation: Quaternion,
    val shape: Shape
)

fun addWallLamp(dice: Dice): (WallLampInput) -> Hand = { (wallPosition, orientation, shape) ->
  val heightOffset = dice.getFloat(2f, 3f) - shape.height / 2f
  val position = wallPosition +
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
}

val wallLampFromHand: (Hand) -> WallLampInput = { hand ->
  val shape = hand.collisionShape!!.shape
  val body = hand.body!!
  WallLampInput(body.position, body.orientation, shape)
}

fun hasLamp(dice: Dice): (Float) -> Boolean = { lampRate ->
  lampRate == 1f || dice.getFloat() <= lampRate
}

// Currently will only return zero or one item but is designed to support multiple because it might come in handy and
// is cleaner to transform empty lists than optional nulls
fun placeWallLamps(dice: Dice, meshes: MeshInfoMap, hands: List<Hand>): List<WallLampInput> {
  val meshQuery = MeshQuery(all = setOf(MeshAttribute.wall, MeshAttribute.canHaveAttachment))
  val matches = meshMatches(meshQuery)
  val options = hands
      .filter { matches(meshes[it.depiction!!.mesh]!!.attributes) }

  return if (options.any()) {
    val hand = dice.takeOne(options)
    listOf(
        wallLampFromHand(hand)
    )
  } else
    listOf()
}

//fun cubeWallLamps(lampRate: Float, heightOffset: Float = 0f) = blockBuilder { input ->
//  val dice = input.general.dice
//  val meshes = input.general.config.meshes
//  val hasLamp = hasLamp(dice)(lampRate)
//  val inputs = if (hasLamp) {
//    val options = boundaryHands.entries
//        .flatMap { (direction, hands) ->
//          hands
//              .map { hand ->
//                val angleZ = directionRotation(oppositeDirections[direction]!!)
//                val body = hand.body!!
//                hand.copy(
//                    body = body.copy(
//                        orientation = Quaternion().rotateZ(angleZ),
//                        position = body.position + Vector3(0f, 0f, heightOffset)
//                    )
//                )
//              }
//        }
//
//    placeWallLamps(dice, meshes, options)
//  } else
//    listOf()
//
//  inputs.map(addWallLamp(dice))
//}

fun withWallLamp(lampRate: Float) = wrapBlockBuilder { input, hands ->
  val dice = input.general.dice
  val meshes = input.general.config.meshes
  val hasLamp = hasLamp(dice)(lampRate)
  if (hasLamp) {
    val lampHands = placeWallLamps(dice, meshes, hands)
        .map{ wallLampInput ->
          wallLampInput.copy(
              orientation = Quaternion(wallLampInput.orientation).rotateZ(-Pi * 0.5f)
          )
        }
    hands.plus(lampHands.map(addWallLamp(dice)))
  } else
    hands
}

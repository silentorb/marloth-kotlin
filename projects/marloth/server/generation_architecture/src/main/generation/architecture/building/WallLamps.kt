package generation.architecture.building

import generation.architecture.misc.BuilderInput
import generation.general.Direction
import marloth.scenery.enums.MeshId
import org.joml.times
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Hand

fun addWallLamp(input: BuilderInput): (Direction, Hand) -> Hand = { direction, hand ->
  val dice = input.general.dice
  val shape = hand.collisionShape!!.shape
  val body = hand.body!!
  val angleZ = directionRotation(direction)
  val heightOffset = dice.getFloat(2f, 3f) - shape.height / 2f
  val orientation = Quaternion().rotateZ(angleZ + Pi * 0f)
//  val orientation = Quaternion(body.orientation).rotateZ(angleZ - Pi * 0.5f)
  val position = body.position +
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

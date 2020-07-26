package generation.architecture.building

import generation.general.Direction
import marloth.scenery.enums.MeshId
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.Hand

fun addWallLamp(position: Vector3, orientation: Quaternion): Hand {
  return Hand(
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = MeshId.wallLamp
      ),
      body = Body(
          position = position,
          orientation = orientation,
          velocity = Vector3()
      )
  )
}

fun cubeWallLamp(direction: Direction, offset: Vector3): Hand {
  val wallPosition = getCubeWallPosition(direction)
  val orientation = getWallOrientation(direction).rotateZ(Pi)
  val position = wallPosition + offset + orientation * Vector3(0.4f, 0f, 0f)
  return addWallLamp(position, orientation)
}

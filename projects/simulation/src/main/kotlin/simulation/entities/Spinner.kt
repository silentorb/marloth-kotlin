package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Quaternion

data class Spinner(
    val rate: Float
)

fun updateSpinnerRotation(spinners: Table<Spinner>, delta: Float, id: Id, orientation: Quaternion): Quaternion {
  val spinner = spinners[id]
  return if (spinner != null)
    orientation.rotateZ(spinner.rate * delta)
  else
    orientation
}

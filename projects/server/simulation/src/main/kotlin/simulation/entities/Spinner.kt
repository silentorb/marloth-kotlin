package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.Body

data class Spinner(
    val rate: Float
)

fun updateSpinnerRotation(spinners: Table<Spinner>, delta: Float): (Id, Body) -> Body = { id, body ->
  val spinner = spinners[id]
  if (spinner != null)
    body.copy(
        orientation = body.orientation.rotateZ(spinner.rate * delta)
    )
  else
    body
}

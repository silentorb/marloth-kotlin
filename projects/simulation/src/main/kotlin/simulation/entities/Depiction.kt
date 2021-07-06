package simulation.entities

import silentorb.mythic.lookinglass.Material
import silentorb.mythic.scenery.MeshName

enum class DepictionType {
  billboard,
  child,
  hound,
  spikyBall,
  monster,
  person,
  none,
  sentinel,
  staticMesh,
  test,
  world
}

data class Depiction(
    val type: DepictionType = DepictionType.staticMesh,
    val mesh: MeshName? = null,
    val material: Material? = null,
)

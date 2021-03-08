package simulation.entities

import silentorb.mythic.scenery.MeshName
import silentorb.mythic.scenery.TextureName

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
    val texture: TextureName? = null
)

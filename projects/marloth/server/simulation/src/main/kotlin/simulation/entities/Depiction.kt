package simulation.entities

import mythic.ent.Id
import scenery.*
import simulation.main.Deck
import simulation.updating.simulationDelta

typealias AnimationDurationMap = Map<ArmatureId, Map<AnimationName, Float>>

enum class DepictionType {
  billboard,
  child,
  spikyBall,
  monster,
  person,
  none,
  staticMesh,
  test,
//  wallLamp,
  world
}

data class Depiction(
    val type: DepictionType,
    val mesh: MeshName? = null,
    val texture: TextureName? = null
)

//fun updateDepiction(deck: Deck, animationDurations: AnimationDurationMap): (Id, Depiction) -> Depiction = { id, depiction ->
//  val animations = getTargetAnimations(deck, id)
//  throw Error("Needs work")
//  if (animations.none())
//    depiction
//  else
//    depiction.copy(
////        animations = updateAnimations(deck, animationDurations, id, animations, simulationDelta)
//    )
//}

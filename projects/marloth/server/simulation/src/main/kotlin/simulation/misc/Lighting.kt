package simulation.misc

import silentorb.mythic.ent.Table
import simulation.entities.Depiction
import simulation.main.Hand
import simulation.main.IdHand

fun lightHandsFromDepictions(lightAttachments: LightAttachmentMap, depictions: Table<Depiction>): List<IdHand> {
  return depictions
      .filterValues { lightAttachments.containsKey(it.mesh) }
      .flatMap { (id, depiction) ->
        val lights = lightAttachments[depiction.mesh]!!
        lights.map { light ->
          IdHand(
              id = id,
              hand = Hand(
                  light = light
              )
          )
        }
      }
}

fun lightHandsFromDepictions(lightAttachments: LightAttachmentMap, hands: List<IdHand>): List<IdHand> {
  return hands
      .filter { lightAttachments.containsKey(it.hand.depiction?.mesh) }
      .flatMap { (id, hand) ->
        val lights = lightAttachments[hand.depiction!!.mesh]!!
        lights.map { light ->
          IdHand(
              id = id,
              hand = Hand(
                  light = light
              )
          )
        }
      }
}

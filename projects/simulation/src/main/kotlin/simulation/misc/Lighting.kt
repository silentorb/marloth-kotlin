package simulation.misc

import simulation.main.Hand
import simulation.main.IdHand

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

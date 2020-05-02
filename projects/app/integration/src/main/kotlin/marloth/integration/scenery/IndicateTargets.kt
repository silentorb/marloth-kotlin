package marloth.integration.scenery

import marloth.scenery.enums.TextureId
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.TexturedBillboard
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck

fun getTargetingLayer(deck: Deck, player: Id): SceneLayer? {
  val target = deck.targets[player]
  return if (target == null)
    null
  else {
    val targetBody = deck.bodies[target]
    if (targetBody != null) {
      SceneLayer(
          elements = listOf(
              ElementGroup(
                  billboards = listOf(
                      TexturedBillboard(
                          texture = TextureId.perlinParticle,
                          position = targetBody.position,
                          scale = 1f,
                          color = Vector4(1f),
                          step = 0
                      )
                  )
              )
          ),
          useDepth = false
      )
    } else
      null
  }
}

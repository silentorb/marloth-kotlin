package marloth.integration.scenery

import marloth.scenery.enums.TextureId
import silentorb.mythic.characters.targeting.TargetTable
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.lookinglass.ElementGroup
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.TexturedBillboard
import silentorb.mythic.spatial.Vector4
import simulation.main.Deck

fun getTargetingLayer(deck: Deck, targetings: TargetTable, player: Id): SceneLayer? {
  val target = targetings[player]
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
                          texture = TextureId.perlinParticle.name,
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

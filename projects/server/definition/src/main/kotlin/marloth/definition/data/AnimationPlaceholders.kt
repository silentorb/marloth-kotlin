package marloth.definition.data

import silentorb.mythic.breeze.AnimationInfo
import silentorb.mythic.breeze.TimelineMarker
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.combat.spatial.attackMarker

object AnimationPlaceholders {
  val cast = AnimationInfo(
      duration = 0.9f,
      markers = listOf(
          TimelineMarker(
              name = attackMarker,
              frame = 40
          )
      )
  )
}

fun animationPlaceholders() =
    reflectPropertiesMap<AnimationInfo>(AnimationPlaceholders)

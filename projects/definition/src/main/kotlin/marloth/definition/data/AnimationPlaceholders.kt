package marloth.definition.data

import silentorb.mythic.breeze.AnimationInfo
import silentorb.mythic.breeze.TimelineMarker
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.combat.spatial.executeMarker

object AnimationPlaceholders {
  val cast = AnimationInfo(
      duration = 0.2f,
      markers = listOf(
          TimelineMarker(
              name = executeMarker,
              frame = 6
          )
      )
  )
}

fun animationPlaceholders() =
    reflectPropertiesMap<AnimationInfo>(AnimationPlaceholders)

package silentorb.mythic.audio

import silentorb.mythic.aura.Sound
import silentorb.mythic.aura.SoundType
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector3

typealias SoundName = String

data class NewSound(
    val type: SoundType,
    val volume: Float,
    val position: Vector3? = null
) : GameEvent

fun soundsFromEvents(events: Events): List<Sound> =
    events.filterIsInstance<NewSound>()
        .map {
          Sound(
              type = it.type,
              volume = it.volume,
              position = it.position,
              progress = 0f
          )
        }

package mythic.aura

import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.pipe
import mythic.platforming.PlatformAudio

data class SoundInfo(
    val id: Id,
    val duration: Long
)

data class Sound(
    val id: Id,
    val type: Id,
    val progress: Long = 0L
)

typealias SoundTable = Table<Sound>

fun updateSounds(audio: PlatformAudio, types: Table<SoundInfo>): (SoundTable) -> SoundTable = { sounds ->
  val bufferSize = audio.bufferSize
  val progress = bufferSize.toLong()

  pipe(sounds, listOf(
      { s ->
        s.mapValues { (_, sound) ->
          sound.copy(
              progress = sound.progress + progress
          )
        }
      },
      { s ->
        s.filterValues { sound ->
          val info = types[sound.type]!!
          sound.progress < info.duration
        }
      }
  ))
}
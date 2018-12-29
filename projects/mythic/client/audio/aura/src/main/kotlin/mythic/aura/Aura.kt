package mythic.aura

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.pipe
import mythic.platforming.PlatformAudio

data class SoundData(
    override val id: Id,
    val buffer: ByteArray,
    val duration: Long
) : Entity

data class Sound(
    override val id: Id,
    val type: Id,
    val progress: Long = 0L
) : Entity

typealias SoundTable = Table<Sound>
typealias SoundLibrary = Table<SoundData>

private var kz = 0L

fun updateSounds(audio: PlatformAudio, library: SoundLibrary): (SoundTable) -> SoundTable = { sounds ->
  val bufferSize = audio.bufferSize
  val progress = bufferSize.toLong()
  val buffer = ByteArray(bufferSize) { i -> Math.sin((kz + i).toDouble() * 0.001f).toByte() }
  kz += bufferSize
  audio.update(buffer)

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
          val info = library[sound.type]!!
          sound.progress < info.duration
        }
      }
  ))
}
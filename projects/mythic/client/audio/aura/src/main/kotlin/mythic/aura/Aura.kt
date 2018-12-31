package mythic.aura

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.pipe
import mythic.platforming.PlatformAudio
import java.nio.ByteBuffer
import java.nio.ShortBuffer

data class SoundData(
    override val id: Id,
    val buffer: ShortBuffer,
    val duration: Double
) : Entity

data class Sound(
    override val id: Id,
    val type: Id,
    val progress: Double = 0.0
) : Entity

data class BufferState(
    val maxSize: Int,
    val bufferedBytes: Int
)

fun newBufferState(audio: PlatformAudio) =
    BufferState(
        maxSize = audio.bufferSize,
        bufferedBytes = 0
    )

typealias SoundTable = Table<Sound>
typealias SoundLibrary = Table<SoundData>

data class AudioState(
    val sounds: SoundTable,
    val buffer: BufferState,
    val nextSoundId: Id = 0L
)

fun newAudioState(audio: PlatformAudio) =
    AudioState(
        sounds = mapOf(),
        buffer = newBufferState(audio)
    )

//fun updateAudioBuffer(state: BufferState, delta: Float): Pair<BufferState, Int> {
//  val samplesPerSecond = 44100
//  val bytesPerSample = 2 * 2
//  return Math.ceil(samplesPerSecond * bytesPerSample * delta).toInt()
//}

private var kz = 0L

fun updateSounds(audio: PlatformAudio, library: SoundLibrary, delta: Float): (SoundTable) -> SoundTable = { sounds ->
  val samplesPerSecond = 44100
  val bytesPerSample = 2 * 2
  val samples = Math.ceil(samplesPerSecond * delta.toDouble()).toInt()
  val bufferSize = bytesPerSample * samples
  val buffer = ByteBuffer.allocate(bufferSize)
  (0 until samples).forEach { i ->
    val value = (Math.sin((kz + i).toDouble() * 0.1) * Short.MAX_VALUE * 0.99).toShort()
    buffer.putShort(value)
    buffer.putShort(value)
  }

  kz += samples
  val b = ByteArray(bufferSize)
  buffer.position(0)
  buffer.get(b)
  audio.update(b)

  pipe(sounds, listOf(
      { s ->
        s.mapValues { (_, sound) ->
          sound.copy(
              progress = sound.progress + delta
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
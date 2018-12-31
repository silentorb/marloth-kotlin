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
    val duration: Long
) : Entity

data class Sound(
    override val id: Id,
    val type: Id,
    val progress: Long = 0L
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

fun audioBufferSamples(delta: Float): Int {
  val samplesPerSecond = 44100
  return Math.ceil(samplesPerSecond * delta.toDouble()).toInt()
}

private var kz = 0L

fun updateSounds(audio: PlatformAudio, library: SoundLibrary, samples: Int): (SoundTable) -> SoundTable = { sounds ->
  val bytesPerSample = 2 * 2
  val bufferSize = bytesPerSample * samples
  val buffer = ByteBuffer.allocate(bufferSize)
  val activeSounds = sounds.values.map { sound ->
    val info = library[sound.type]!!
    Triple(info.duration - sound.progress, sound.progress, info.buffer)
  }
  (0 until samples).forEach { i ->
    //    val value = (Math.sin((kz + i).toDouble() * 0.1) * Short.MAX_VALUE * 0.99).toShort()
    val value: Short = activeSounds
        .filter { i < it.first }
        .map { it.third.get(it.second.toInt() + i) }
        .sum()
        .toShort()

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
              progress = sound.progress + samples
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
package mythic.aura

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.pipe
import mythic.platforming.PlatformAudio
import mythic.spatial.Vector3
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
    val progress: Long = 0L,
    val position: Vector3? = null
) : Entity

data class BufferState(
    val maxSize: Int,
    val bufferedBytes: Int
)

fun newBufferState(audio: PlatformAudio) =
    BufferState(
        maxSize = audio.availableBuffer,
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

private data class CalculatedSound(
    val remainingSamples: Int,
    val progress: Int,
    val buffer: ShortBuffer,
    val gain: Float
)

val maxSoundRange = 40f

fun toDb(value: Float): Float {
  val base = 10
  val result = (Math.pow(base.toDouble(), value.toDouble()) - 1) / (base - 1)
  return result.toFloat()
}

fun applyDistanceAttenuation(listenerPosition: Vector3?, sound: Sound, info: SoundData): Float =
    if (sound.position != null) {
      if (listenerPosition == null)
        0f
      else {
        val distance = listenerPosition.distance(sound.position) - 2f
        if (distance < 0f)
          1f
        else
          1f - Math.min(1f, distance / maxSoundRange)
//        toDb(1f - Math.min(1f, distance / maxSoundRange))
      }
    } else
      1f

private val cutoff: Int = (Short.MAX_VALUE * 0.7f).toInt()

fun applyCutoff(value: Int): Int =
    cutoff + (value - cutoff) / 8

fun compress(value: Int): Int {
  return when {
    value > cutoff -> applyCutoff(value)
    value < -cutoff -> -applyCutoff(-value)
    else -> value
  }
}

fun updateSounds(audio: PlatformAudio, library: SoundLibrary, samples: Int, listenerPosition: Vector3?): (SoundTable) -> SoundTable = { sounds ->
  val bytesPerSample = 2 * 2
  val bufferSize = bytesPerSample * samples
  val buffer = ByteBuffer.allocate(bufferSize)
  val activeSounds = sounds.values
      .map { sound ->
        val info = library[sound.type]!!
        val gain = applyDistanceAttenuation(listenerPosition, sound, info)

        CalculatedSound(
            remainingSamples = (info.duration - sound.progress).toInt(),
            progress = sound.progress.toInt(),
            buffer = info.buffer,
            gain = gain
        )
      }
      .filter { it.gain > 0f }

  (0 until samples).forEach { i ->
    //    val value = (Math.sin((kz + i).toDouble() * 0.1) * Short.MAX_VALUE * 0.99).toShort()
    val value: Int = activeSounds
        .filter { i < it.remainingSamples }
        .map { (it.buffer.get(it.progress + i) * it.gain).toInt() }
        .sum()

    val compressedValue = compress(value)

    if (compressedValue < Short.MIN_VALUE || compressedValue > Short.MAX_VALUE) {
      println("Exceeded buffer $value -> $compressedValue")
    }
    buffer.putShort(compressedValue.toShort())
    buffer.putShort(compressedValue.toShort())
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
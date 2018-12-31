package mythic.desktop

import mythic.platforming.PlatformAudio
import org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename
import org.lwjgl.system.MemoryStack.*
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

fun defaultAudioFormat(): AudioFormat =
    AudioFormat(
        44100f,
        16,
        2,
        true,
        true // big endian like Java
    )

fun loadSoundFromFile(filename: String): ShortBuffer {

//Allocate space to store return information from the function
  stackPush()
  val channelsBuffer = stackMallocInt(1)
  stackPush()
  val sampleRateBuffer = stackMallocInt(1)

  val rawAudioBuffer = stb_vorbis_decode_filename(filename, channelsBuffer, sampleRateBuffer)

//Retreive the extra information that was stored in the buffers by the function
  val channels = channelsBuffer.get()
  val sampleRate = sampleRateBuffer.get()

//Free the space we allocated earlier
  stackPop()
  stackPop()

  assert(rawAudioBuffer != null)
  assert(channels == 1)
  assert(sampleRate == 44100)
  return rawAudioBuffer
}

class DesktopAudio : PlatformAudio {
  var sourceLine: SourceDataLine? = null
  val format = defaultAudioFormat()

  override fun start() {
    val devices = AudioSystem.getMixerInfo()
    if (devices.any()) {
      val source = AudioSystem.getSourceDataLine(format)!!
      source.open(format)
      source.start()
      sourceLine = source
    }
  }

  override val bufferSize: Int
    get() {
      val source = sourceLine
      return if (source != null) {
        val result = source.available()
        result
      } else
        0
    }

  override fun update(bytes: ByteArray): Int {
    val source = sourceLine
    return if (source != null) {
      source.write(bytes, 0, bytes.size)
    } else
      0
  }

  override fun stop() {
    val source = sourceLine
    if (source != null) {
      source.drain()
      source.close()
    }
  }

  override fun loadSound(filename: String): ShortBuffer =
      loadSoundFromFile(filename)
}
package mythic.desktop

import mythic.platforming.PlatformAudio
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
}
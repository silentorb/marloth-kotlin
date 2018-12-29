package mythic.desktop

import mythic.platforming.PlatformAudio
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

fun defaultAudioFormat(): AudioFormat =
    AudioFormat(
        44100f,
        16,
        1,
        true,
        true // big endian like Java
    )

class DesktopAudio : PlatformAudio {
  var sourceLine: SourceDataLine? = null
  val format = defaultAudioFormat()

  override fun start() {
//    val devices = AudioSystem.getMixerInfo()
//    if (devices.any()) {
//      val source = AudioSystem.getSourceDataLine(format)!!
//      sourceLine = source
//    }
  }


  override val bufferSize: Int
    get() {
      val source = AudioSystem.getSourceDataLine(format)
      return if (source != null) {
        source.open(format)
        val result = source.available()
        source.close()
        result
      } else
        0
    }

  override fun update(bytes: ByteArray): Int {
    val source = AudioSystem.getSourceDataLine(format)
//    val source = sourceLine
    return if (source != null) {
      try {
        source.open(format)
        source.start()
        source.write(bytes, 0, bytes.size)
      }
      finally {
        source.drain()
        source.close()
      }
    } else
      0
  }

  override fun stop() {
//    val source =  AudioSystem.getSourceDataLine(format)
//    if (source != null) {
//      source.close()
//    }
  }
}
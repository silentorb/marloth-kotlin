package mythic.desktop

import mythic.platforming.PlatformAudio
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

fun defaultAudioFormat(): AudioFormat =
    AudioFormat(
        AudioFormat.Encoding.PCM_FLOAT,
        44100f,
        16,
        2, // stereo channels
        0,
        0f,
        true // big endian like Java
    )

class DesktopAudio : PlatformAudio {
  var sourceLine: SourceDataLine? = null

  override fun start() {
    val devices = AudioSystem.getMixerInfo()
    if (devices.any()) {
      val source = AudioSystem.getSourceDataLine(defaultAudioFormat())!!
      sourceLine = source
      source.start()
    }
  }

  override val bufferSize: Int = 0

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
      source.stop()
    }
  }
}
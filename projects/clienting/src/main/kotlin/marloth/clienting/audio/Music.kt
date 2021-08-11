package marloth.clienting.audio

import silentorb.mythic.debugging.getDebugString
import java.io.File
import java.io.FileInputStream
import javax.sound.midi.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

data class MusicPlayer(
    val sequencer: Sequencer,
    val synthesizer: Synthesizer,
) {
  fun start() {
    sequencer.start()
  }

  fun stop() {
    sequencer.stop()
    synthesizer.close()
    sequencer.close()
  }
}

fun isAudioDeviceAvailable(): Boolean {
  val format = AudioFormat(44100f, 16, 2, true, false)
  val info = DataLine.Info(SourceDataLine::class.java, format)
  return AudioSystem.isLineSupported(info)
}

fun newMusicPlayer(): MusicPlayer? {
  if (!isAudioDeviceAvailable())
    return null

  val soundfontPath = getDebugString("SOUNDFONT_PATH") ?: return null
  val midiFilePath = getDebugString("MIDI_PATH") ?: return null
  val soundfont: Soundbank = MidiSystem.getSoundbank(FileInputStream(File(soundfontPath)))

  val sequence = Sequence(0f, 120)
  val track = sequence.createTrack()
  val sequencer = MidiSystem.getSequencer()
  val synthesizer = MidiSystem.getSynthesizer()

  sequencer.open()
  synthesizer.open()
  synthesizer.loadAllInstruments(soundfont)

  sequencer.transmitter.receiver = synthesizer.receiver
  sequencer.sequence = sequence
//  sequencer.setSequence(FileInputStream(File(midiFilePath)))
  val s = sequencer.sequence

//  sequencer.

  return MusicPlayer(
      sequencer = sequencer,
      synthesizer = synthesizer,
  )
}

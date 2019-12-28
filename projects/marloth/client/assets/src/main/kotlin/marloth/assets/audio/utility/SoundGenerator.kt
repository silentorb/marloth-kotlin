package marloth.assets.audio.utility

interface SoundGenerator {
  fun init(samplingFrequency: Int)
  fun compute(count: Int, inputs: Array<FloatArray?>?, outputs: Array<FloatArray?>?)
}

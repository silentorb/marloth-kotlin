package scenery

enum class Sounds {
  girlScream,
  hogAmbient01,
  hogAmbient02,
  hogAmbient03,
  hogDeath
}

fun soundId(sound: Sounds): Long = sound.ordinal.toLong()
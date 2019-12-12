package marloth.scenery.enums

enum class Sounds {
  girlScream,
  hogAmbient01,
  hogAmbient02,
  hogAmbient03,
  hogDeath,
  throwWeapon
}

fun soundId(sound: Sounds): Long = sound.ordinal.toLong()

package marloth.scenery.enums

enum class DamageTypes {
  //  chaos,
  cold,
  //  lightning,
  fire,
  physical,
  poison
}

val staticDamageTypes = DamageTypes.values().map { it.name }

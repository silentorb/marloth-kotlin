package marloth.scenery.enums

enum class ModifierType {
  damageMultiplier,
  _notSpecified
}

enum class ModifierDirection {
  none,
  incoming,
  outgoing
}

enum class ModifierId {
  damageBurning,
  damageChilled,
  damagePoisoned,
  resistanceCold,
  resistanceFire,
  resistancePoison
}

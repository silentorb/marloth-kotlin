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

object ModifierId {
  const val damageBurning = "damageBurning"
  const val damageChilled = "damageChilled"
  const val damagePoisoned = "damagePoisoned"
  const val entangled = "entangled"
  const val entangleImmune = "entangledImmune"
  const val entangling = "entangling"
  const val mobile = "mobile"
  const val resistanceCold = "resistanceCold"
  const val resistanceFire = "resistanceFire"
  const val resistancePoison = "resistancePoison"
}

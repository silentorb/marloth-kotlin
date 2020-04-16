package silentorb.mythic.combat.general

typealias Percentage = Int

val applyMultiplier: (Int, Percentage) -> Int = { value, mod ->
  value * mod / 100
}

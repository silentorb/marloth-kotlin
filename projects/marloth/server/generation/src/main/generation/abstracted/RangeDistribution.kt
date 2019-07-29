package generation.abstracted

import randomly.Dice

fun <T> normalizeRanges(slotCount: Int, ranges: Map<T, Int>): Map<T, Int> {
  val rangeTotal = ranges.values.sum()
  val initialNormalized = ranges.mapValues { it.value * slotCount / rangeTotal }
  val initialNormalizedTotal = initialNormalized.values.sum()
  val gap = slotCount - initialNormalizedTotal
  assert(gap >= 0)
  assert(gap <= ranges.size)
  val finalNormalized = if (gap > 0)
    initialNormalized.plus(
        initialNormalized.entries
            .sortedByDescending { it.value }
            .take(gap)
            .map { Pair(it.key, it.value + 1) }
            .associate { it }
    )
  else
    initialNormalized

  val finalNormalizedTotal = finalNormalized.values.sum()
  assert(finalNormalizedTotal == slotCount)
  return finalNormalized
}

fun <T> distributeToSlots(dice: Dice, slotCount: Int, ranges: Map<T, Int>): List<T> {
  val normalized = normalizeRanges(slotCount, ranges)
  val pool = normalized.flatMap { range ->
    (0 until range.value).map { range.key }
  }
  return dice.scramble(pool)
}

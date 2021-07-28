package simulation.misc

data class Fraction(
    val numerator: Int,
    val denominator: Int,
) {
  operator fun times(value: Int): Int =
      value * numerator / denominator
}

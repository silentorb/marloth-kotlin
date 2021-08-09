package simulation.accessorize

data class Nutrient(
    val value: Int, // Percentage
)

const val replenishmentKey = "replenishmentKey"

// This is an experimental feature
data class IntrinsicReplenishment(
    val delay: Int = 1,
    val counter: Int = 0,
)

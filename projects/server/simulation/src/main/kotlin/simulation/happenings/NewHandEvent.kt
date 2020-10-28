package simulation.happenings

import simulation.main.Hand

// Does not support IdHand
data class NewHandEvent(
    val hand: Hand
)

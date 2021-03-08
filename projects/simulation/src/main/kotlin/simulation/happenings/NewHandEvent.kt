package simulation.happenings

import simulation.main.Hand

@Deprecated("Use NewHand")
data class NewHandEvent(
    val hand: Hand
)

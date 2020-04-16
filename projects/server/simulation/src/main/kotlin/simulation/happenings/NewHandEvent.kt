package simulation.happenings

import silentorb.mythic.happenings.GameEvent
import simulation.main.Hand

// Does not support IdHand
data class NewHandEvent(
    val hand: Hand
): GameEvent

package silentorb.mythic.combat

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

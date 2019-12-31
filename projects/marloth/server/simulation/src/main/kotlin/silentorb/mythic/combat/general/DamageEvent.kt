package silentorb.mythic.combat.general

import silentorb.mythic.combat.general.Damage
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

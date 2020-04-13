package silentorb.mythic.combat.general

import silentorb.mythic.combat.general.Damage
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent

data class DamageEvent(
    val damage: Damage,
    val target: Id
) : GameEvent

fun newDamageEvents(target: Id, source: Id,damages: List<DamageDefinition>): Events =
    damages.map { damage ->
      DamageEvent(
          target = target,
          damage = Damage(
              type = damage.type,
              amount = damage.amount,
              source = source
          )
      )
    }

package silentorb.mythic.combat.general

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector3

data class DamageEvent(
    val damage: Damage,
    val target: Id,
    val position: Vector3? = null
) : GameEvent

fun newDamageEvents(target: Id, source: Id, damages: List<DamageDefinition>, position: Vector3? = null): Events =
    damages.map { damage ->
      DamageEvent(
          target = target,
          position = position,
          damage = Damage(
              type = damage.type,
              amount = damage.amount,
              source = source
          )
      )
    }

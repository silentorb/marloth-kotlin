package simulation.combat.general

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3

data class DamageEvent(
    val damage: Damage,
    val target: Id,
    val position: Vector3? = null
)

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

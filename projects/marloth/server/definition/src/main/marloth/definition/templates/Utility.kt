package marloth.definition.templates

import simulation.combat.DamageType
import simulation.evention.Action
import simulation.evention.ApplyBuff
import simulation.evention.DamageAction
import simulation.evention.Trigger
import simulation.main.Hand

fun damageBuff(damageType: DamageType) = { action: Action ->
  Hand(
      trigger = Trigger(
          action = DamageAction(
              damageType = damageType,
              amount = (action as ApplyBuff).strength
          )
      )
  )
}

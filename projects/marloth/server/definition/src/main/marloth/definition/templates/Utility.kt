package marloth.definition.templates

import simulation.combat.DamageType
import simulation.happenings.Action
import simulation.happenings.ApplyBuff
import simulation.happenings.DamageAction
import simulation.happenings.Trigger
import simulation.main.Hand

//fun damageBuff(damageType: DamageType) = { action: Action ->
//  Hand(
//      trigger = Trigger(
//          action = DamageAction(
//              damageType = damageType,
//              amount = (action as ApplyBuff).strength
//          )
//      )
//  )
//}

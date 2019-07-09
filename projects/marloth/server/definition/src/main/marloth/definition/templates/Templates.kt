package marloth.definition.templates

import marloth.definition.BuffId
import simulation.combat.DamageType
import simulation.evention.ApplyBuff
import simulation.evention.DamageAction
import simulation.evention.Trigger
import simulation.main.Hand
import simulation.main.HandTemplates

val templates: HandTemplates = mapOf(
    BuffId.poisoned.name to { action ->
      Hand(
          trigger = Trigger(
              action = DamageAction(
                  damageType = DamageType.poison,
                  amount = (action as ApplyBuff).strength
              )
          )
      )
    }
)

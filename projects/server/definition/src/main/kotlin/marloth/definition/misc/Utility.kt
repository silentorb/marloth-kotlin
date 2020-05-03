package marloth.definition.misc

import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.Text
import simulation.combat.general.DamageType
import simulation.combat.general.ModifierOperation
import silentorb.mythic.accessorize.ModifierDefinition
import simulation.combat.general.ValueModifier
import simulation.combat.general.ValueModifierDirection

fun newResistanceModifier(name: Text, damageType: DamageType) = ModifierDefinition(
    name = name,
    direction = ModifierDirection.incoming,
    valueModifier = ValueModifier(
        operation = ModifierOperation.multiply,
        direction = ValueModifierDirection.minus,
        subtype = damageType
    )
)

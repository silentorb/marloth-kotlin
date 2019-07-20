package marloth.definition

import scenery.enums.ModifierDirection
import scenery.enums.Text
import simulation.combat.DamageType
import simulation.combat.ModifierOperation
import simulation.entities.ModifierDefinition
import simulation.misc.ValueModifier
import simulation.misc.ValueModifierDirection

fun newResistanceModifier(name: Text, damageType: DamageType) = ModifierDefinition(
    name = name,
    direction = ModifierDirection.incoming,
    valueModifier = ValueModifier(
        operation = ModifierOperation.multiply,
        direction = ValueModifierDirection.minus,
        subtype = damageType
    )
)

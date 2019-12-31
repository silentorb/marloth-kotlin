package marloth.definition

import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.Text
import silentorb.mythic.combat.general.DamageType
import silentorb.mythic.combat.general.ModifierOperation
import silentorb.mythic.accessorize.ModifierDefinition
import silentorb.mythic.combat.general.ValueModifier
import silentorb.mythic.combat.general.ValueModifierDirection

fun newResistanceModifier(name: Text, damageType: DamageType) = ModifierDefinition(
    name = name,
    direction = ModifierDirection.incoming,
    valueModifier = ValueModifier(
        operation = ModifierOperation.multiply,
        direction = ValueModifierDirection.minus,
        subtype = damageType
    )
)

package marloth.definition.misc

import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.Text
import silentorb.mythic.editing.PropertyDefinitions
import silentorb.mythic.editing.loadGraphLibrary
import silentorb.mythic.ent.reflectPropertiesMap
import simulation.accessorize.ModifierDefinition
import simulation.combat.general.DamageType
import simulation.combat.general.ModifierOperation
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

val staticDamageTypes = reflectPropertiesMap<String>(DamageTypes).keys

fun loadMarlothGraphLibrary(propertyDefinitions: PropertyDefinitions) =
    loadGraphLibrary(propertyDefinitions, "world")

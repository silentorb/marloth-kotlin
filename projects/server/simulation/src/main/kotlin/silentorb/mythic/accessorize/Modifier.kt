package silentorb.mythic.accessorize

import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.ModifierType
import marloth.scenery.enums.Text
import simulation.combat.general.ValueModifier
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.EventTrigger

data class Modifier(
    val type: AccessoryName,
    val strength: Int = 1,
    val target: Id,
    val source: Id? = null
)

data class ChildAccessory(
    val type: AccessoryName,
    val level: Int
)

//fun modifierToRelative(modifier: Modifier) =
//    RelativeModifier(
//        type = modifier.type,
//        strength = modifier.strength
//    )

data class ModifierDefinition(
    val name: Text,
    val type: ModifierType = ModifierType._notSpecified,
    val direction: ModifierDirection = ModifierDirection.none,
    val overTime: EventTrigger? = null,
    val valueModifier: ValueModifier? = null
)

package marloth.definition.misc

import marloth.definition.data.ActionAccessory
import marloth.definition.data.ActionAccessoryMap
import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.ModifierDirection
import marloth.scenery.enums.Text
import silentorb.mythic.editing.main.loadGraphLibrary
import silentorb.mythic.ent.*
import silentorb.mythic.scenery.SceneProperties
import simulation.accessorize.ModifierDefinition
import simulation.combat.general.DamageType
import simulation.combat.general.ModifierOperation
import simulation.combat.general.ValueModifier
import simulation.combat.general.ValueModifierDirection
import simulation.misc.GameAttributes
import simulation.misc.GameProperties

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

fun loadMarlothGraphLibrary(propertiesSerialization: PropertiesSerialization) =
    loadGraphLibrary(propertiesSerialization, "world")

fun loadMarlothDefinitions(propertiesSerialization: PropertiesSerialization): GraphStores =
    loadGraphLibrary(propertiesSerialization, "world/src/entities")
        .mapValues { SimpleGraphStore((it.value as AnyGraph).toSet()) }

fun multiLevelActionAccessory(baseName: String, levels: Int, generator: (Int) -> ActionAccessory): ActionAccessoryMap =
    (1..levels)
        .associate { level ->
          val name = if (level < 2)
            baseName
          else
            "$baseName$level"

          val bundle = generator(level)
          val bundleWithAnyUpgrades = if (level < levels)
            bundle.copy(
                accessory = bundle.accessory.copy(
                    upgrades = setOf("$baseName${level + 1}"),
                    level = level,
                )
            )
          else
            bundle

          name to bundleWithAnyUpgrades
        }

fun isBlockSide(entry: Entry): Boolean =
    (entry.property == SceneProperties.type && entry.target == GameAttributes.blockSide) ||
        entry.property == GameProperties.mine

fun getSideNodes(graph: Graph): Collection<String> =
    graph
        .filter(::isBlockSide)
        .map { it.source }
        .distinct()

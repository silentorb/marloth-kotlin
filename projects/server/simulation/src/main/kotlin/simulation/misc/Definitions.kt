package simulation.misc

import marloth.scenery.enums.TextResourceMapper
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName
import silentorb.mythic.aura.SoundType
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.ent.GraphLibrary
import silentorb.mythic.ent.GraphStores
import simulation.combat.general.DamageType
import simulation.combat.general.WeaponDefinition
import silentorb.mythic.particles.ParticleEffectDefinitions
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.performing.ActionDefinition
import silentorb.mythic.scenery.Shape
import simulation.characters.CharacterDefinition

typealias AccessoryDefinitions = Map<AccessoryName, AccessoryDefinition>
typealias LightAttachmentMap = Map<MeshName, List<Light>>
typealias Professions = Map<String, CharacterDefinition>

data class ApplicationInfo(
    val version: String
)

data class Definitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val actions: Map<AccessoryName, ActionDefinition>,
    val animations: AnimationInfoMap,
    val applicationInfo: ApplicationInfo,
    val damageTypes: Set<DamageType>,
    val lightAttachments: LightAttachmentMap,
    val particleEffects: ParticleEffectDefinitions,
    val professions: Professions,
    val selectableAccessories: Set<AccessoryName>,
    val soundDurations: Map<SoundType, Float>,
    val textLibrary: TextResourceMapper,
    val weapons: Map<AccessoryName, WeaponDefinition>,
    val meshShapeMap: Map<String, Shape>,
    val graphs: GraphStores,
)

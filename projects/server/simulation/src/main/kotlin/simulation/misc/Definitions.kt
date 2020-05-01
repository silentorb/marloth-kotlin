package simulation.misc

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.ModifierDefinition
import silentorb.mythic.aura.SoundType
import silentorb.mythic.breeze.AnimationInfoMap
import simulation.combat.general.DamageType
import simulation.combat.general.WeaponDefinition
import silentorb.mythic.particles.ParticleEffectDefinitions
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.MeshName
import silentorb.mythic.performing.ActionDefinition
import simulation.characters.CharacterDefinition

typealias AccessoryDefinitions = Map<AccessoryName, AccessoryDefinition>
typealias LightAttachmentMap = Map<MeshName, List<Light>>
typealias Professions = Map<String, CharacterDefinition>

data class Definitions(
    val accessories: Map<AccessoryName, AccessoryDefinition>,
    val actions: Map<AccessoryName, ActionDefinition>,
    val animations: AnimationInfoMap,
    val damageTypes: Set<DamageType>,
    val lightAttachments: LightAttachmentMap,
    val modifiers: Map<ModifierId, ModifierDefinition>,
    val particleEffects: ParticleEffectDefinitions,
    val professions: Professions,
    val soundDurations: Map<SoundType, Float>,
    val weapons: Map<AccessoryName, WeaponDefinition>
)

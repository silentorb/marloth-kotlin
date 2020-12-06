package marloth.definition.misc

import silentorb.mythic.aura.SoundDurations
import silentorb.mythic.breeze.AnimationInfoMap
import silentorb.mythic.scenery.Shape
import simulation.misc.LightAttachmentMap

data class ClientDefinitions(
    val animations: AnimationInfoMap,
    val lightAttachments: LightAttachmentMap,
    val soundDurations: SoundDurations,
    val meshShapeMap: Map<String, Shape>,
)

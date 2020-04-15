package marloth.definition

import silentorb.mythic.aura.SoundDurations
import silentorb.mythic.breeze.AnimationInfoMap
import simulation.misc.LightAttachmentMap

data class ClientDefinitions(
    val animations: AnimationInfoMap,
    val lightAttachments: LightAttachmentMap,
    val soundDurations: SoundDurations
)

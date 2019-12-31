package silentorb.mythic.combat.spatial

import silentorb.mythic.physics.BulletState

data class SpatialCombatWorld(
    val definitions: SpatialCombatDefinitions,
    val deck: SpatialCombatDeck,
    val bulletState: BulletState
)

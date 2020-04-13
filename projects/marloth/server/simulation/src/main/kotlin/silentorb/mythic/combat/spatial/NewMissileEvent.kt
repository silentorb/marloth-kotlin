package silentorb.mythic.combat.spatial

import silentorb.mythic.combat.general.DamageDefinition
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3

data class NewMissileEvent(
    val position: Vector3,
    val orientation: Quaternion,
    val force: Vector3,
    val damages: List<DamageDefinition>,
    val attacker: Id
): GameEvent

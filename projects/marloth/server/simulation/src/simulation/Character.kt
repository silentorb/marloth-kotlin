package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.times
import physics.Body
import scenery.DepictionType

data class CharacterDefinition(
    val health: Int,
    val abilities: List<AbilityDefinition>,
    val depictionType: DepictionType
)

data class Character(
    val id: Int,
    val turnSpeed: Vector2,
    val body: Body,
    val abilities: MutableList<Ability> = mutableListOf(),
    val faction: Faction,
    val health: Resource,
    val isAlive: Boolean = true,
    val facingRotation: Vector3 = Vector3(),
    val lookVelocity: Vector2 = Vector2()
) {
  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0

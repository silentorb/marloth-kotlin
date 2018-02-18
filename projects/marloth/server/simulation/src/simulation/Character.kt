package simulation

import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.times

data class CharacterDefinition(
    val health: Int,
    val abilities: List<AbilityDefinition>
)

class Character(
    val id: Int,
    val body: Body,
    maxHealth: Int,
    val abilities: MutableList<Ability> = mutableListOf(),
    val faction: Faction
) {
  val health = Resource(maxHealth)
  var isAlive = true
  var facingRotation: Vector3 = Vector3()

  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

fun isFinished(world: World, character: Character) =
    character.health.value == 0
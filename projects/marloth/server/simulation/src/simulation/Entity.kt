package simulation

typealias Id = Int

enum class EntityType {
  character,
  furnishing,
  missile
}

class Entity(
    val id: Id,
    val type: EntityType
)

interface EntityLike {
  val id: Id
}
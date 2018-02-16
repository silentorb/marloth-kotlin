package simulation

typealias Id = Int

enum class EntityType {
  character,
  missile
}

class Entity(
    val id: Id,
    val type: EntityType
)
package simulation

typealias Id = Long

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

typealias IdSource = () -> Id

fun newIdSource(initialValue: Id): IdSource {
  var nextId: Id = initialValue
  return { nextId++ }
}
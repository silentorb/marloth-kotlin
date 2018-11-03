package mythic.ent

typealias Id = Long

interface Entity {
  val id: Id
}typealias IdSource = () -> Id

fun <T : Entity> entityMap(list: Collection<T>): Map<Id, T> =
    list.associate { Pair(it.id, it) }
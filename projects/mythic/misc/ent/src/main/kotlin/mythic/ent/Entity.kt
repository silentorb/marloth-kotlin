package mythic.ent

typealias Id = Long
typealias Table<T> = Map<Id, T>

interface Entity {
  val id: Id
}

typealias IdSource = () -> Id

fun <T : Entity> entityMap(list: Collection<T>): Map<Id, T> =
    list.associate { Pair(it.id, it) }

fun newIdSource(initialValue: Id): IdSource {
  var nextId: Id = initialValue
  return { nextId++ }
}

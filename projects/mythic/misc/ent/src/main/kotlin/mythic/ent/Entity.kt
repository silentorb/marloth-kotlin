package mythic.ent

typealias Id = Long

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

fun <T> pipe(initial: T, steps: List<(T) -> T>): T =
    steps.fold(initial) { a, b -> b(a) }

package mythic.bloom

typealias ParentArranger = (List<Bounds>) -> Bounds
typealias ChildArranger = (Bounds) -> Bounds
typealias Appearance = (Bounds) -> Unit
typealias EventEmitter = (Bounds) -> Any

//data class ParentFlower(
//    val arranger: ParentArranger?,
//    val appearance: Appearance?,
//    val emitter: EventEmitter?
//)
//
//data class ChildFlower(
//    val arranger: ChildArranger,
//    val appearance: Appearance?,
//    val emitter: EventEmitter?
//)

data class Garden(
    val nextId: Id,
    val parentArrangers: Map<Id, ParentArranger>,
    val childArrangers: Map<Id, ChildArranger>,
    val appearances: Map<Id, Appearance>,
    val emitters: Map<Id, EventEmitter>
)

val newParentFlower = {}
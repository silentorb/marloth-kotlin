package metaview

import mythic.ent.Id

enum class EventType {
  inputValueChanged,
  refresh,
  nodeSelect,
  textureSelect
}

data class InputValueChange(
    val node: Id,
    val input: String,
    val value: Any
)

data class Event(
    val type: EventType,
    val data: Any = 0,
    val preview: Boolean = false
)

typealias Emitter = (Event) -> Unit
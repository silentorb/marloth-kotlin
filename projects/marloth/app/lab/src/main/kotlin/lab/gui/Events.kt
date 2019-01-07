package lab.gui

enum class EventType {
  textureSelect
}

data class Event(
    val type: EventType,
    val data: Any
)

typealias Emitter = (Event) -> Unit
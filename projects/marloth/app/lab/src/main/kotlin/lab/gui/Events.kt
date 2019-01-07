package lab.gui

enum class EventType {
  refresh,
  textureSelect
}

data class Event(
    val type: EventType,
    val data: Any = 0
)

typealias Emitter = (Event) -> Unit
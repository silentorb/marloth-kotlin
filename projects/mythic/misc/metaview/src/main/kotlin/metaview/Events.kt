package metaview

enum class EventType {
  addNode,
  connecting,
  deleteSelected,
  inputValueChanged,
  refresh,
  renameTexture,
  newTexture,
  selectInput,
  selectNode,
  textureSelect
}

data class Event(
    val type: EventType,
    val data: Any = 0,
    val preview: Boolean = false
)

typealias Emitter = (Event) -> Unit

data class Renaming(
    val previousName: String,
    val newName: String
)
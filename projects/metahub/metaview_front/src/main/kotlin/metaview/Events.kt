package metaview

enum class EventType {
  addNode,
  connecting,
  deleteSelected,
  duplicateNode,
  inputValueChanged,
  insertNode,
  redo,
  refresh,
  renameTexture,
  newTexture,
  selectInput,
  selectNode,
  setPreviewFinal,
  setTilePreview,
  switchDomain,
  textureSelect,
  undo
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
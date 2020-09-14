package marloth.clienting

enum class ClientEventType {

  //  Display Options
  setWindowMode,

  // Display Options Workflow
  previewDisplayChanges,
  revertDisplayChanges,
}

data class ClientEvent(
    val type: ClientEventType,
    val data: Any? = null
)

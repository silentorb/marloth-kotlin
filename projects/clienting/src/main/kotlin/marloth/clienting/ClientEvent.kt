package marloth.clienting

enum class ClientEventType {

  menuBack,
  menuReplace,
  navigate,

  //  Display Options
  setWindowMode,

  // Display Options Workflow
  saveDisplayChange,
  revertDisplayChanges,
}

data class ClientEvent(
    val type: Any,
    val data: Any? = null,
    val player: Int? = null
)

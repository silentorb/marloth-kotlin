package marloth.clienting

enum class ClientEventType {

  menuBack,
  menuReplace,
  navigate,

  //  Display Options
  setStagingWindowMode,
  setStagingWindowedResolution,
  setStagingFullscreenResolution,

  // Display Options Workflow
  saveDisplayChange,
  revertDisplayChanges,

  setWorldGraph,
}

data class ClientEvent(
    val type: Any,
    val data: Any? = null,
    val user: Any? = null
)

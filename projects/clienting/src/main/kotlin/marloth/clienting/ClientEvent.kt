package marloth.clienting

import silentorb.mythic.happenings.Command

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

//data class ClientEvent(
//    val type: Any,
//    val value: Any? = null,
//    val target: Any? = null
//)

typealias ClientEvent = Command

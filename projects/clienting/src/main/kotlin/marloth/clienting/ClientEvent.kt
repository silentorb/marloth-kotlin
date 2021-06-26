package marloth.clienting

import silentorb.mythic.happenings.Command

enum class ClientEventType {

  navigate,
  menuBack,
  menuReplace,
  drillDown,

  //  Display Options
  setStagingWindowMode,
  setStagingWindowedResolution,
  setStagingFullscreenResolution,

  // Display Options Workflow
  saveDisplayChange,
  revertDisplayChanges,

  setWorldGraph,
}

typealias ClientEvent = Command

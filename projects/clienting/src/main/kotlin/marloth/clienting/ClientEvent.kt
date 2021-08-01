package marloth.clienting

import silentorb.mythic.happenings.Command

object ClientEventType {

  val navigate = "navigate"
  val menuBack = "menuBack"
  val menuReplace = "menuReplace"
  val drillDown = "drillDown"

  //  Display Options
  val setStagingWindowMode = "setStagingWindowMode"
  val setStagingWindowedResolution = "setStagingWindowedResolution"
  val setStagingFullscreenResolution = "setStagingFullscreenResolution"
  val setOption = "SetOption"

  // Display Options Workflow
  val saveDisplayChange = "saveDisplayChange"
  val revertDisplayChanges = "revertDisplayChanges"

  val setWorldGraph = "setWorldGraph"
}

typealias ClientEvent = Command

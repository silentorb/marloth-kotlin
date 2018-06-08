package junk_client

import haft.CommandHandler
import haft.Commands
import haft.ProfileStates
import mythic.platforming.Platform

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()

  fun getWindowInfo() = platform.display.getInfo()

  fun render(state: AppState) {
    val windowInfo = getWindowInfo()
    val canvas = createCanvas(windowInfo)
    renderScene(renderer, state.world!!, canvas, windowInfo)
  }
}

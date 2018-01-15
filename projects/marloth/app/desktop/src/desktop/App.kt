package desktop

import front.runApp
import mythic.desktop.createDesktopPlatform

object App {
  @JvmStatic
  fun main(args: Array<String>) {
    runApp(createDesktopPlatform("Marloth", 800, 600))
  }
}
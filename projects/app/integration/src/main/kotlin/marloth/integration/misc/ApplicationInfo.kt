package marloth.integration.misc

import simulation.misc.ApplicationInfo
import java.util.*

fun loadApplicationInfo(): ApplicationInfo {
  val properties = Properties()
  properties.load(ApplicationInfo::javaClass.javaClass.classLoader.getResourceAsStream("application.properties"))
  return ApplicationInfo(
      version = properties.getProperty("version")
  )
}

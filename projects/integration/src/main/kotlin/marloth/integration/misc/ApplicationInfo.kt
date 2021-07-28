package marloth.integration.misc

import silentorb.mythic.debugging.getDebugString
import simulation.misc.ApplicationInfo
import java.util.*

fun loadApplicationInfo(): ApplicationInfo {
  val properties = Properties()
  properties.load(ApplicationInfo::javaClass.javaClass.classLoader.getResourceAsStream("application.properties"))
  val version = properties.getProperty("version")
  return ApplicationInfo(
      version = if (version == "\$version")
        getDebugString("VERSION") ?: "dev"
      else
        version
  )
}

package desktop

import org.tinylog.configuration.Configuration
import silentorb.mythic.debugging.getDebugString

fun mapLogProperty(environmentVariable: String, property: String) {
  val value = getDebugString(environmentVariable)
  if (value != null)
    Configuration.set(property, value)
}

// Applying environment variables here instead of in the tinylog.properties file
// in order to support dotenv.
fun configureLogging() {
  mapLogProperty("LOG_TARGET", "writer")
  mapLogProperty("LOG_FILE", "writer.file")
  mapLogProperty("LOG_LEVEL", "writer.level")
}

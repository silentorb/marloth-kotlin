package mythic.ent

private val DEBUG_MODE = System.getenv("DEBUG_MODE") != null

private var privateLoopNumber = 0

val globalDebugLoopNumber = privateLoopNumber

fun setGlobalLoopNumber(value: Int) {
  privateLoopNumber = value
}

fun incrementGlobalDebugLoopNumber(max: Int) {
  privateLoopNumber = ++privateLoopNumber % max
}

fun getDebugSetting(name: String): String? =
//    if (DEBUG_MODE)
    System.getenv(name)
//    else
//      null

fun debugLog(message: String) {
  println("($privateLoopNumber) $message")
}

package marloth.clienting

enum class ClientEventType {

  //  Display Options
  setWindowMode,

}

data class ClientEvent(
    val type: ClientEventType,
    val data: Any? = null
)

package junk_simulation.data

import junk_simulation.CharacterType

class Characters {
  companion object {
    val wait = CharacterType(
        name ="Hero"
    )
  }
}

val characterLibrary = Characters::class.java.kotlin.members.toList()
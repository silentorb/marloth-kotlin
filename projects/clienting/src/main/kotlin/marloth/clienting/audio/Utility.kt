package marloth.clienting.audio

import marloth.scenery.enums.Sounds

fun <T> ifTrue(result: T, assertion: () -> Boolean): T? =
    if (assertion())
      result
    else
      null

fun <T> filterNewSounds(argument: Pair<T, T>, entries: List<Pair<(Pair<T, T>) -> Boolean, Sounds>>): List<Sounds> =
    entries
        .mapNotNull { (condition, sound) ->
          if (condition(argument))
            sound
          else
            null
        }

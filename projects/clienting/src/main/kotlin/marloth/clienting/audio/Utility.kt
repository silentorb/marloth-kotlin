package marloth.clienting.audio

import marloth.scenery.enums.SoundId

fun <T> ifTrue(result: T, assertion: () -> Boolean): T? =
    if (assertion())
      result
    else
      null

fun <T> filterNewSounds(argument: Pair<T, T>, entries: List<Pair<(Pair<T, T>) -> Boolean, SoundId>>): List<SoundId> =
    entries
        .mapNotNull { (condition, sound) ->
          if (condition(argument))
            sound
          else
            null
        }

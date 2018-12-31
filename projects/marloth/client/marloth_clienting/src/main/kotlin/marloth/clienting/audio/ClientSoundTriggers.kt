package marloth.clienting.audio

import marloth.clienting.ClientState
import marloth.clienting.gui.ViewId
import scenery.Sounds

fun <T> ifTrue(result: T, assertion: () -> Boolean): T? =
    if (assertion())
      result
    else
      null

fun getClientSounds(previous: ClientState, next: ClientState): List<Sounds> =
    listOfNotNull(
        ifTrue(Sounds.girlScream) { previous.view != ViewId.mainMenu && next.view == ViewId.mainMenu }
    )
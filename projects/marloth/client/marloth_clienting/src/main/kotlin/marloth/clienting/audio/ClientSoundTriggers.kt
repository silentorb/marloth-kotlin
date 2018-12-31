package marloth.clienting.audio

import marloth.clienting.ClientState
import marloth.clienting.gui.ViewId
import scenery.Sounds

fun newClientSounds(previous: ClientState, next: ClientState): List<Sounds> =
    listOf()
//    listOfNotNull(
//        ifTrue(Sounds.girlScream) { previous.view != ViewId.mainMenu && next.view == ViewId.mainMenu }
//    )
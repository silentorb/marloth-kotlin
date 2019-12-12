package marloth.clienting.audio

import marloth.clienting.ClientState
import mythic.aura.Sound
import mythic.ent.IdSource
import mythic.ent.Table
import marloth.scenery.enums.Sounds

fun newClientSounds(nextId: IdSource, previous: ClientState, next: ClientState): Table<Sound> =
    listOf<Sounds>()
        .associate {
          val id = nextId()
          val sound = Sound(
              id = id,
              type = it.ordinal.toLong()
          )
          Pair(id, sound)
        }
//    listOfNotNull(
//        ifTrue(Sounds.girlScream) { previous.view != ViewId.mainMenu && next.view == ViewId.mainMenu }
//    )

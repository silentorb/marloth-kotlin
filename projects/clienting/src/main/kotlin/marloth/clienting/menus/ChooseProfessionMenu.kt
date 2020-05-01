package marloth.clienting.menus

import marloth.definition.data.availableProfessions
import silentorb.mythic.ent.Id
import simulation.characters.NewPlayerCharacter

fun chooseProfessionMenu(player: Id): List<SimpleMenuItem> = availableProfessions()
    .map { (id, definition) ->
      SimpleMenuItem(
          text = definition.name,
          event = GuiEvent(
              type = GuiEventType.gameEvent,
              data = NewPlayerCharacter(
                  id = player,
                  profession = id
              )
          )
      )
    }

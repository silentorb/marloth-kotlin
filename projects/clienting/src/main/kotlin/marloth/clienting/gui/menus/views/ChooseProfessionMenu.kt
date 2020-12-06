package marloth.clienting.gui.menus.views

import marloth.clienting.gui.menus.SimpleMenuItem
import marloth.clienting.gui.menus.newSimpleMenuItem
import marloth.definition.data.availableProfessions
import silentorb.mythic.ent.Id
import simulation.characters.NewPlayerCharacter

fun chooseProfessionMenu(player: Id): List<SimpleMenuItem> =
    availableProfessions()
        .map { (id, definition) ->
          newSimpleMenuItem(
              text = definition.name,
              event = NewPlayerCharacter(
                  id = player,
                  profession = id
              )
          )
        }

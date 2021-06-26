package marloth.clienting.gui.menus.views.character

import marloth.clienting.ClientEventType
import marloth.clienting.gui.menus.general.SimpleMenuItem
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import marloth.definition.data.availableProfessions
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import simulation.characters.NewPlayerCharacter

fun chooseProfessionMenu(player: Id): List<SimpleMenuItem> =
    availableProfessions()
        .map { (id, definition) ->
          newSimpleMenuItem(
              definition.name,
              NewPlayerCharacter(
                  id = player,
                  profession = id
              ),
              Command(ClientEventType.navigate, null, player)
          )
        }

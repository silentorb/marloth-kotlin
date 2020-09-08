package marloth.clienting.menus.views

import marloth.clienting.menus.ClientOrServerEvent
import marloth.clienting.menus.SimpleMenuItem
import marloth.definition.data.availableProfessions
import silentorb.mythic.ent.Id
import simulation.characters.NewPlayerCharacter

fun chooseProfessionMenu(player: Id): List<SimpleMenuItem> =
    availableProfessions()
        .map { (id, definition) ->
          SimpleMenuItem(
              text = definition.name,
              event = ClientOrServerEvent(
                  server = NewPlayerCharacter(
                      id = player,
                      profession = id
                  )
              )
          )
        }

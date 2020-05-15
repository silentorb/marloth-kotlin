package marloth.clienting.menus

import silentorb.mythic.accessorize.ChoseImprovedAccessory
import silentorb.mythic.ent.Id
import simulation.characters.AccessoryOptions
import simulation.misc.Definitions

fun chooseAccessoryMenu(definitions: Definitions, actor: Id, accessoryOptions: AccessoryOptions): List<SimpleMenuItem> =
    accessoryOptions
        .map { id ->
          val definition = definitions.accessories[id]!!
          SimpleMenuItem(
              text = definition.name,
              event = GuiEvent(
                  type = GuiEventType.gameEvent,
                  data = ChoseImprovedAccessory(
                      actor = actor,
                      accessory = id
                  )
              )
          )
        }

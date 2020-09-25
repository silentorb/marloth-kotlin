package marloth.clienting.gui.menus.views

import marloth.clienting.gui.menus.SimpleMenuItem
import marloth.clienting.gui.menus.newSimpleMenuItem
import simulation.accessorize.ChooseImprovedAccessory
import silentorb.mythic.ent.Id
import simulation.characters.AccessoryOptions
import simulation.misc.Definitions

fun chooseAccessoryMenu(definitions: Definitions, actor: Id, accessoryOptions: AccessoryOptions): List<SimpleMenuItem> =
    accessoryOptions
        .map { id ->
          val definition = definitions.accessories[id]!!
          newSimpleMenuItem(
              text = definition.name,
              event = ChooseImprovedAccessory(
                  actor = actor,
                  accessory = id
              )
          )
        }

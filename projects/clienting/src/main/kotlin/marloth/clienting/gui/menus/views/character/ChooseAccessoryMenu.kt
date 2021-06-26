package marloth.clienting.gui.menus.views.character

import marloth.clienting.gui.menus.general.SimpleMenuItem
import marloth.clienting.gui.menus.general.newSimpleMenuItem
import simulation.accessorize.ChooseImprovedAccessory
import silentorb.mythic.ent.Id
import simulation.characters.AccessoryOptions
import simulation.misc.Definitions

fun chooseAccessoryMenu(definitions: Definitions, actor: Id, accessoryOptions: AccessoryOptions): List<SimpleMenuItem> =
    accessoryOptions
        .map { id ->
          val definition = definitions.accessories[id]!!
          newSimpleMenuItem(
              definition.name,
              ChooseImprovedAccessory(
                  actor = actor,
                  accessory = id
              )
          )
        }

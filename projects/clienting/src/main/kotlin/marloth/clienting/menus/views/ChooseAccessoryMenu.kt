package marloth.clienting.menus.views

import marloth.clienting.menus.EventUnion
import marloth.clienting.menus.SimpleMenuItem
import simulation.accessorize.ChooseImprovedAccessory
import silentorb.mythic.ent.Id
import simulation.characters.AccessoryOptions
import simulation.misc.Definitions

fun chooseAccessoryMenu(definitions: Definitions, actor: Id, accessoryOptions: AccessoryOptions): List<SimpleMenuItem> =
    accessoryOptions
        .map { id ->
          val definition = definitions.accessories[id]!!
          SimpleMenuItem(
              text = definition.name,
              event = ChooseImprovedAccessory(
                  actor = actor,
                  accessory = id
              )
          )
        }

package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.DevText
import simulation.characters.CharacterDefinition
import simulation.entities.ClearAreaTask
import simulation.entities.ContractDefinition
import simulation.entities.DepictionType
import simulation.entities.Ware
import simulation.misc.Entities

fun characterDefinitions(): Map<String, CharacterDefinition> = mapOf(

    Entities.farmer to CharacterDefinition(
        name = DevText("Farmer"),
        depictionType = DepictionType.child,
        availableContracts = listOf(
            ContractDefinition(
                name = DevText("Accursed Farm"),
                tasks = listOf(
                    ClearAreaTask(
                        zone = "farm"
                    )
                ),
                reward = 20,
            )
        )
    ),

    Entities.grocer to CharacterDefinition(
        name = DevText("Grocer Gal"),
        depictionType = DepictionType.child,
        wares = listOf(
            Ware(
                type = AccessoryId.cookie,
                price = 10,
                quantity = 10,
            )
        )
    ),

    )

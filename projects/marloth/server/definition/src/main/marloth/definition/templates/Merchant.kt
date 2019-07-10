package marloth.definition.templates

import marloth.definition.creatures
import mythic.ent.IdSource
import mythic.spatial.Vector3
import scenery.Text
import simulation.main.Hand
import simulation.misc.Interactable
import simulation.misc.WidgetCommand
import simulation.misc.newCharacter

fun newMerchant(nextId: IdSource, position: Vector3): Hand =
    newCharacter(
        nextId = nextId,
        position = position,
        definition = creatures.merchant,
        faction = 1
    )
        .copy(
            interactable = Interactable(
                primaryCommand = WidgetCommand(
                    text = Text.talk
                )
            )
        )

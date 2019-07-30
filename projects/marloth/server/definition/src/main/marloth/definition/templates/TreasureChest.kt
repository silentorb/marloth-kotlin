package marloth.definition.templates

import mythic.spatial.Vector3
import scenery.enums.MeshId
import scenery.enums.ResourceId
import scenery.enums.Text
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Interactable
import simulation.entities.WidgetCommand
import simulation.happenings.TakeItem
import simulation.main.Hand
import simulation.misc.MeshInfoMap
import simulation.misc.ResourceBundle
import simulation.physics.Body
import simulation.physics.CollisionObject

fun newTreasureChest(meshInfo: MeshInfoMap, position: Vector3, amount: Int): Hand =
    Hand(
        body = Body(
            position = position
        ),
        depiction = Depiction(
            type = DepictionType.staticMesh,
            mesh = MeshId.treasureChest.toString()
        ),
        collisionShape = CollisionObject(shape = meshInfo[MeshId.treasureChest.toString()]!!),
        interactable = Interactable(
            primaryCommand = WidgetCommand(
                text = Text.gui_take,
                action = TakeItem()
            )
        ),
        resources = ResourceBundle(
            values = mapOf(
                ResourceId.money to amount
            )
        )
    )

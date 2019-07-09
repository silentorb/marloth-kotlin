package marloth.definition

import scenery.Box
import scenery.ShapeOffset
import scenery.Sphere
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import simulation.physics.CollisionObject
import simulation.physics.DynamicBody
import simulation.physics.HingeConstraint
import scenery.Text
import simulation.main.Hand
import simulation.misc.*

class EntityTemplates {
  companion object {

    val door = Hand(
        collisionShape = CollisionObject(Box(halfExtents = Vector3(.5f, .165f / 2f, .5f))),
        door = Door(
            isLocked = false
        ),
        dynamicBody = DynamicBody(
            gravity = false,
            mass = 45f,
            resistance = 4f,
            hinge = HingeConstraint(
                pivot = Vector3(0.55f, 0f, 0f),
                axis = Vector3(0f, 0f, 1f)
            )
        ),
        interactable = Interactable(
            primaryCommand = WidgetCommand(
                text = Text.open
            ),
            secondaryCommand = WidgetCommand(
                text = Text.close
            )
        )
    )

    val wallLamp = Hand(
        collisionShape = CollisionObject(
            shape = ShapeOffset(Matrix().translate(1f, 0f, 0.8f), Sphere(1f))
        ),
        depiction = Depiction(
            type = DepictionType.wallLamp
        ),
        light = Light(
            color = Vector4(1f),
            range = 15f
        )
    )

  }
}

package marloth.definition

import silentorb.mythic.scenery.Box
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.physics.HingeConstraint
import marloth.scenery.enums.Text
import simulation.entities.*
import simulation.main.Hand

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
                text = Text.menu_open
            ),
            secondaryCommand = WidgetCommand(
                text = Text.menu_close
            )
        )
    )

//    val wallLamp = Hand(
//        collisionShape = CollisionObject(
//            shape = ShapeOffset(Matrix().translate(1f, 0f, 0.8f), Sphere(1f))
//        ),
//        depiction = Depiction(
//            type = DepictionType.wallLamp
//        )
////        ,
////        light = Light(
////            color = Vector4(1f),
////            range = 15f
////        )
//    )

  }
}

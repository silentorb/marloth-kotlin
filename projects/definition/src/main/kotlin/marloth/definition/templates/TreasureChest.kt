package marloth.definition.templates

import marloth.scenery.enums.MeshInfoMap
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.Text
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Interactable
import simulation.entities.WidgetCommand
import simulation.happenings.TakeItem
import simulation.main.Hand
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import simulation.physics.CollisionGroups

fun newTreasureChest(meshInfo: MeshInfoMap, position: Vector3, amount: Int): Hand {
  val shape = meshInfo[MeshId.treasureChest.toString()]!!.shape
  return Hand(
      body = Body(
          position = position
      ),
      collisionShape = if (shape != null)
        CollisionObject(
            shape = shape,
            groups = CollisionGroups.static,
            mask = CollisionGroups.staticMask
        )
      else
        null,
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = MeshId.treasureChest.toString()
      ),
      interactable = Interactable(
          primaryCommand = WidgetCommand(
              text = Text.gui_take,
              action = TakeItem()
          )
      ),
//      resources = ResourceBundle(
//          values = mapOf(
//              ResourceId.money.name to amount
//          )
//      )
  )
}

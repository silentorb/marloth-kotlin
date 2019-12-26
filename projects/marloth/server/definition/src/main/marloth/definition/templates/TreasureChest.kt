package marloth.definition.templates

import generation.architecture.misc.MeshInfoMap
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.ResourceId
import marloth.scenery.enums.Text
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.entities.Interactable
import simulation.entities.WidgetCommand
import simulation.happenings.TakeItem
import simulation.main.Hand
import silentorb.mythic.combat.ResourceBundle
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject

fun newTreasureChest(meshInfo: MeshInfoMap, position: Vector3, amount: Int): Hand {
  val shape = meshInfo[MeshId.treasureChest.toString()]!!.shape
  return Hand(
      body = Body(
          position = position
      ),
      depiction = Depiction(
          type = DepictionType.staticMesh,
          mesh = MeshId.treasureChest.toString()
      ),
      collisionShape = if (shape != null) CollisionObject(shape = shape) else null,
      interactable = Interactable(
          primaryCommand = WidgetCommand(
              text = Text.gui_take,
              action = TakeItem()
          )
      ),
      resources = ResourceBundle(
          values = mapOf(
              ResourceId.money.name to amount
          )
      )
  )
}

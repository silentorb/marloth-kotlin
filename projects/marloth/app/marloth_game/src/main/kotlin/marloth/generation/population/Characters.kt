package marloth.generation.population

import generation.architecture.building.floorOffset
import generation.architecture.misc.applyCellPosition
import marloth.definition.creatures
import marloth.definition.newCharacter
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import marloth.scenery.enums.AccessoryId
import simulation.entities.*
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.main.Hand
import simulation.main.HandAttachment
import simulation.misc.*
import silentorb.mythic.rigging.characters.getLookAtAngle

fun placeAiCharacter(definitions: Definitions, faction: Id, definition: CharacterDefinition, position: Vector3): Hand {
  return newCharacter(definitions,
      definition = definition,
      faction = faction,
      position = position,
      spirit = Spirit(
          pursuit = Pursuit()
      )
  )
}

fun placeEnemy(definitions: Definitions, node: Node): Hand =
    placeAiCharacter(definitions,
        faction = monsterFaction,
        definition = creatures.monster,
        position = node.position
    )

fun newPlayer(definitions: Definitions, grid: MapGrid, cellPosition: Vector3i): Hand {
  val neighbor = cellNeighbors(grid.connections, cellPosition).first()
  return newCharacter(definitions,
      definition = creatures.player,
      faction = misfitFaction,
      position = applyCellPosition(cellPosition) + floorOffset + Vector3(0f, 0f, 6f),
      angle = getLookAtAngle((neighbor - cellPosition).toVector3())
  )
      .copy(
          attachments = listOf(
              HandAttachment(
                  category = AttachmentCategory.equipped,
                  index = 2,
                  hand = Hand(
                      accessory = Accessory(
                          type = AccessoryId.candle.name
                      )
                  )
              )
          ),
          player = Player(
              name = "Unknown Hero"
          )
      )
}

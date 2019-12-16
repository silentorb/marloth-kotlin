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
import simulation.misc.*
import silentorb.mythic.characters.getLookAtAngle
import silentorb.mythic.ent.IdSource
import simulation.main.IdHand

fun placeAiCharacter(nextId: IdSource, definitions: Definitions, faction: Id, definition: CharacterDefinition, position: Vector3): List<IdHand> {
  return newCharacter(nextId, nextId(), definitions,
      definition = definition,
      faction = faction,
      position = position,
      spirit = Spirit(
          pursuit = Pursuit()
      )
  )
}

fun placeEnemy(nextId: IdSource, definitions: Definitions, node: Node): List<IdHand> =
    placeAiCharacter(nextId, definitions,
        faction = monsterFaction,
        definition = creatures.monster,
        position = node.position
    )

fun newPlayer(nextId: IdSource, definitions: Definitions, grid: MapGrid, cellPosition: Vector3i): List<IdHand> {
  val neighbor = cellNeighbors(grid.connections, cellPosition).first()
  val character = nextId()
  return newCharacter(nextId, character, definitions,
      definition = creatures.player,
      faction = misfitFaction,
      position = applyCellPosition(cellPosition) + floorOffset + Vector3(0f, 0f, 6f),
      angle = getLookAtAngle((neighbor - cellPosition).toVector3())
  )
      .plus(
          IdHand(
              id = character,
              hand = Hand(
                  player = Player(
                      name = "Unknown Hero"
                  )
              )
          )
      )
      .plus(
          IdHand(
              id = nextId(),
              hand = Hand(
                  accessory = Accessory(
                      type = AccessoryId.candle.name
                  ),
                  attachment = Attachment(
                      target = character,
                      index = 2,
                      category = AttachmentCategory.equipped
                  )
              )
          )
      )
}

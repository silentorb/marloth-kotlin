package marloth.generation.population

import simulation.misc.floorOffset
import marloth.definition.data.creatures
import marloth.definition.newCharacter
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.toVector3
import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.Accessory
import simulation.entities.*
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.main.Hand
import simulation.misc.*
import silentorb.mythic.characters.getHorizontalLookAtAngle
import silentorb.mythic.ent.IdSource
import simulation.intellect.assessment.Knowledge
import simulation.intellect.freshSpirit
import simulation.main.IdHand

fun placeAiCharacter(nextId: IdSource, definitions: Definitions, faction: Id, definition: CharacterDefinition, position: Vector3): List<IdHand> {
  return newCharacter(nextId, nextId(), definitions,
      definition = definition,
      faction = faction,
      position = position,
      spirit = freshSpirit()
  )
}

fun placeEnemy(nextId: IdSource, definitions: Definitions, cell: Vector3, definition: CharacterDefinition): List<IdHand> =
    placeAiCharacter(nextId, definitions,
        faction = monsterFaction,
        definition = definition,
        position = cell
    )

fun newPlayer(nextId: IdSource, definitions: Definitions, grid: MapGrid, cellPosition: Vector3i): List<IdHand> {
  val neighbor = cellNeighbors(grid.connections, cellPosition).first()
  val character = nextId()
  return newCharacter(nextId, character, definitions,
      definition = creatures.player,
      faction = misfitFaction,
      position = absoluteCellPosition(cellPosition) + floorOffset + Vector3(0f, 0f, 6f),
      angle = getHorizontalLookAtAngle((neighbor - cellPosition).toVector3())
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
//      .plus(
//          IdHand(
//              id = nextId(),
//              hand = Hand(
//                  accessory = Accessory(
//                      type = AccessoryId.candle.name,
//                      target = character
//                  )
//              )
//          )
//      )
}

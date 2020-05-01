package simulation.characters

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.getHorizontalLookAtAngle
import silentorb.mythic.spatial.toVector3
import simulation.entities.Player
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.*

data class NewPlayerCharacter(
    val id: Id,
    val profession: ProfessionId
): GameEvent

fun newPlayerIdHand(id: Id) =
    IdHand(
        id = id,
        hand = Hand(
            player = Player(
                name = "Unknown Hero"
            )
        )
    )

fun getDebugProfession() =
    getDebugString("CHARACTER_CLASS")

fun newPlayerCharacter(nextId: IdSource, id: Id, definitions: Definitions, grid: MapGrid, profession: ProfessionId, cellPosition: Vector3i): List<IdHand> {
  val neighbor = cellNeighbors(grid.connections, cellPosition).first()
  return newCharacter(nextId, id, definitions,
      profession = profession,
      faction = misfitFaction,
      position = absoluteCellPosition(cellPosition) + floorOffset + Vector3(0f, 0f, 6f),
      angle = getHorizontalLookAtAngle((neighbor - cellPosition).toVector3())
  )
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid, cellPosition: Vector3i, profession: ProfessionId): List<IdHand> {
  val id = nextId()
  return newPlayerCharacter(nextId, id, definitions, grid, profession, cellPosition)
      .plus(newPlayerIdHand(id))
}

fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, grid: MapGrid, events: Events): List<IdHand> =
    events.filterIsInstance<NewPlayerCharacter>()
        .flatMap { event ->
          newPlayerCharacter(nextId, event.id, definitions, grid, event.profession, getPlayerStart(grid))
        }

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid): List<IdHand> {
  val debugProfession = getDebugProfession()
  return if (debugProfession != null)
    newPlayerAndCharacter(nextId, definitions, grid, getPlayerStart(grid), debugProfession)
  else
    listOf(newPlayerIdHand(nextId()))
}

package simulation.characters

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.spatial.getYawAngle
import silentorb.mythic.spatial.toVector3
import simulation.entities.Player
import simulation.main.Hand
import simulation.main.IdHand
import simulation.misc.*

data class NewPlayerCharacter(
    val id: Id,
    val profession: ProfessionId
)

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
  val neighbor = cellNeighbors(grid.connections, cellPosition).firstOrNull()
  return if (neighbor == null)
    listOf()
  else
    newCharacter(nextId, id, definitions,
        profession = profession,
        faction = misfitFaction,
        position = absoluteCellPosition(cellPosition) + Vector3(0f, 0f, 1f),
        angle = getYawAngle((neighbor - cellPosition).toVector3())
    )
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid, cellPosition: Vector3i, profession: ProfessionId): List<IdHand> {
  val id = nextId()
  return newPlayerCharacter(nextId, id, definitions, grid, profession, cellPosition)
      .plus(newPlayerIdHand(id))
}

fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, grid: MapGrid, events: Events): List<IdHand> {
  val playerStart = getPlayerStart(grid)
  return if (playerStart == null)
    listOf()
  else
    events.filterIsInstance<NewPlayerCharacter>()
        .flatMap { event ->
          newPlayerCharacter(nextId, event.id, definitions, grid, event.profession, playerStart)
        }
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid): List<IdHand> {
  val debugProfession = getDebugProfession()
  val playerStart = getPlayerStart(grid)
  return if (debugProfession != null && playerStart != null)
    newPlayerAndCharacter(nextId, definitions, grid, playerStart, debugProfession)
  else
    listOf(newPlayerIdHand(nextId()))
}

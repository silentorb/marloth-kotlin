package simulation.characters

import silentorb.mythic.characters.rigs.newThirdPersonRig
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.*
import silentorb.mythic.ent.scenery.toSpatialEntries
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3
import simulation.entities.Player
import simulation.main.NewHand
import simulation.misc.*

data class NewPlayerCharacter(
    val id: Id,
    val profession: ProfessionId
)

fun newPlayerIdHand(id: Id) =
    NewHand(
        id = id,
        components = listOf(
            Player(
                name = "Unknown Hero",
                rig = id,
            )
        )
    )

fun getDebugProfession() =
    getDebugString("PLAYER_PROFESSION")

fun newPlayerCharacter(nextId: IdSource, id: Id, definitions: Definitions, profession: ProfessionId, location: Vector3, angle: Float): NewHand {
  return newCharacter(id, definitions,
      definition = definitions.professions[profession]!!,
      faction = Factions.misfits,
      position = location + Vector3(0f, 0f, 1f),
      angle = angle
  ).plusComponents(newThirdPersonRig(location, angle))
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, location: Vector3, angle: Float, profession: ProfessionId): List<NewHand> {
  val id = nextId()
  return listOf(
      newPlayerCharacter(nextId, id, definitions, profession, location, angle)
  )
      .plus(newPlayerIdHand(id))
}

//fun newPlayerCharacter(id: Id, definitions: Definitions, grid: MapGrid, profession: ProfessionId, cellPosition: Vector3i): List<NewHand> {
//  val neighbor = cellNeighbors(grid.connections, cellPosition).firstOrNull()
//  return if (neighbor == null)
//    listOf()
//  else
//    listOf(
//        newCharacter(id, definitions,
//            definition = definitions.professions[profession]!!,
//            faction = Factions.misfits,
//            position = absoluteCellPosition(cellPosition) + Vector3(0f, 0f, 1f),
//            angle = getYawAngle((neighbor - cellPosition).toVector3())
//        )
//    )
//}

//fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid, cellPosition: Vector3i, profession: ProfessionId): List<NewHand> {
//  val id = nextId()
//  return newPlayerCharacter(id, definitions, grid, profession, cellPosition)
//      .plus(newPlayerIdHand(id))
//}

fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, graph: Graph, events: Events): List<NewHand> {
  return events.filterIsInstance<NewPlayerCharacter>()
      .mapNotNull { event ->
        val playerStart = getPlayerStart(graph)
        if (playerStart == null)
          null
        else
          newPlayerCharacter(nextId, event.id, definitions, event.profession, playerStart.translation(), playerStart.rotation().z)
      }
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, graph: Graph): List<NewHand> {
  val debugProfession = getDebugProfession()
  val playerStart = getPlayerStart(graph)
  return if (debugProfession != null && playerStart != null)
    newPlayerAndCharacter(nextId, definitions, playerStart.translation(), playerStart.rotation().z, debugProfession)
  else
    listOf(newPlayerIdHand(nextId()))
}

fun newPlayerAndCharacter(definitions: Definitions, staticGraph: Graph): GraphStore {
  val debugProfession = getDebugProfession()
  val playerStart = getPlayerStart(staticGraph)
  val definition = definitions.graphs[debugProfession]
  return if (debugProfession != null && definition != null && playerStart != null)
    definition + toSpatialEntries(playerStart, debugProfession)
  else
    emptyGraphStore
}

//fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, grid: MapGrid, events: Events): List<NewHand> {
//  val playerStart = getPlayerStart(grid)
//  return if (playerStart == null)
//    listOf()
//  else
//    events.filterIsInstance<NewPlayerCharacter>()
//        .flatMap { event ->
//          newPlayerCharacter(nextId, event.id, definitions, grid, event.profession, playerStart)
//        }
//}

//fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, grid: MapGrid): List<NewHand> {
//  val debugProfession = getDebugProfession()
//  val playerStart = getPlayerStart(grid)
//  return if (debugProfession != null && playerStart != null)
//    newPlayerAndCharacter(nextId, definitions, grid, playerStart, debugProfession)
//  else
//    listOf(newPlayerIdHand(nextId()))
//}

package simulation.characters

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3
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

fun newPlayerCharacter(nextId: IdSource, id: Id, definitions: Definitions, profession: ProfessionId, location: Vector3, angle: Float): List<IdHand> {
  return newCharacter(nextId, id, definitions,
      profession = profession,
      faction = misfitFaction,
      position = location + Vector3(0f, 0f, 1f),
      angle = angle
  )
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, location: Vector3, angle: Float, profession: ProfessionId): List<IdHand> {
  val id = nextId()
  return newPlayerCharacter(nextId, id, definitions, profession, location, angle)
      .plus(newPlayerIdHand(id))
}

fun newPlayerCharacters(nextId: IdSource, definitions: Definitions, graph: Graph, events: Events): List<IdHand> {
  val playerStart = getPlayerStart(graph)
  return if (playerStart == null)
    listOf()
  else
    events.filterIsInstance<NewPlayerCharacter>()
        .flatMap { event ->
          newPlayerCharacter(nextId, event.id, definitions, event.profession, playerStart.translation(), playerStart.rotation().z)
        }
}

fun newPlayerAndCharacter(nextId: IdSource, definitions: Definitions, graph: Graph): List<IdHand> {
  val debugProfession = getDebugProfession()
  val playerStart = getPlayerStart(graph)
  return if (debugProfession != null && playerStart != null)
    newPlayerAndCharacter(nextId, definitions, playerStart.translation(), playerStart.rotation().z, debugProfession)
  else
    listOf(newPlayerIdHand(nextId()))
}

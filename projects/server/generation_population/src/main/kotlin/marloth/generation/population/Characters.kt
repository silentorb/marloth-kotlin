package marloth.generation.population

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.IdSource
import silentorb.mythic.spatial.Vector3
import simulation.characters.ProfessionId
import simulation.characters.newCharacter
import simulation.intellect.freshSpirit
import simulation.main.IdHand
import simulation.misc.Definitions
import simulation.misc.Factions

fun placeAiCharacter(nextId: IdSource, definitions: Definitions, faction: Id, profession: ProfessionId, position: Vector3): List<IdHand> {
  return newCharacter(nextId, nextId(), definitions,
      profession = profession,
      faction = faction,
      position = position,
      spirit = freshSpirit()
  )
}

fun placeEnemy(nextId: IdSource, definitions: Definitions, cell: Vector3, profession: ProfessionId): List<IdHand> =
    placeAiCharacter(nextId, definitions,
        faction = Factions.monsters,
        profession = profession,
        position = cell
    )

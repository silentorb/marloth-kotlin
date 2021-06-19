package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import simulation.main.NewHand
import simulation.misc.DistAttributes

fun distributeLightSlots(slots: SlotMap): SlotMap {
  val slotLocations = slots.map { it.value.transform.translation() }
  val notUsed = removeClumps(10f, slotLocations)
  return slots.filter { (_, slot) ->
    notUsed.none { it == slot.transform.translation() }
  }
}

fun distributeLightHands(nextId: IdSource, config: GenerationConfig, dice: Dice, slots: SlotMap): List<NewHand> {
  return slots.flatMap { (_, slot) ->
    val slotAttribute = if (slot.attributes.contains(SlotTypes.ground))
      DistAttributes.floor
    else
      DistAttributes.wall

    val definitions = filterPropGraphs(config, setOf(slotAttribute, DistAttributes.light))
    val definition = dice.takeOne(definitions)
    val k = graphToHands(config.resourceInfo.meshShapes, nextId, definition, slot.transform)
    k
  }
}

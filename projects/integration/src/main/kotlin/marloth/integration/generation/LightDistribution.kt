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

fun distributeLightSlots(config: DistributionConfig, slots: SlotMap): SlotMap =
    distributeLightSlots(slots)

fun distributeLightHands(config: GenerationConfig, nextId: IdSource, dice: Dice, slots: SlotMap): List<NewHand> {
  return slots.flatMap { (_, slot) ->
    slotToHands(config, nextId, dice, slot) { it.contains(DistAttributes.light) }
  }
}

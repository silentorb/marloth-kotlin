package simulation.updating

import silentorb.mythic.aura.updateSound
import silentorb.mythic.combat.general.updateDestructibleHealth
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.mapTableValues
import silentorb.mythic.happenings.Events
import simulation.entities.*
import simulation.intellect.updateSpirit
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import silentorb.mythic.timing.updateFloatCycle
import silentorb.mythic.timing.updateFloatTimer
import silentorb.mythic.timing.updateIntCycle
import silentorb.mythic.timing.updateIntTimers
import simulation.intellect.assessment.lightRatings
import simulation.intellect.assessment.updateKnowledge
import simulation.happenings.updateActions
import simulation.particles.updateParticleEffect
import simulation.physics.updateMarlothCharacterRig

fun updateEntities(definitions: Definitions, world: World, events: Events): (Deck) -> Deck =
    { deck ->
      val delta = simulationDelta
      val dice = world.dice
      deck.copy(
          actions = updateActions(world.definitions, deck, events),
          ambientSounds = updateAmbientAudio(dice, deck),
          animations = mapTable(deck.animations, updateCharacterAnimation(deck, definitions.animations, delta)),
          characterRigs = mapTable(deck.characterRigs, updateMarlothCharacterRig(world.bulletState, deck, events)),
          attachments = mapTable(deck.attachments, updateAttachment(events)),
          cyclesFloat = mapTableValues(deck.cyclesFloat, updateFloatCycle(delta)),
          cyclesInt = mapTableValues(deck.cyclesInt, updateIntCycle),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events)),
          characters = mapTable(deck.characters, updateCharacter(deck, world.bulletState, events)),
          knowledge = mapTable(deck.knowledge, updateKnowledge(world, lightRatings(world.deck), delta)),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(definitions.particleEffects, dice, delta)),
          sounds = mapTableValues(deck.sounds, updateSound(delta)),
          spirits = mapTable(deck.spirits, updateSpirit(world, delta)),
          timersInt = updateIntTimers(events)(deck.timersInt),
          timersFloat = mapTableValues(deck.timersFloat, updateFloatTimer(delta))
      )
    }

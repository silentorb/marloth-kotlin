package simulation.updating

import silentorb.mythic.aura.updateSound
import silentorb.mythic.combat.general.updateDestructibleHealth
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.mapTableValues
import silentorb.mythic.happenings.Events
import simulation.entities.*
import simulation.intellect.updateAiState
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import silentorb.mythic.particles.updateParticleEffect
import silentorb.mythic.timing.updateFloatCycle
import silentorb.mythic.timing.updateFloatTimer
import silentorb.mythic.timing.updateIntCycle
import silentorb.mythic.timing.updateIntTimers
import simulation.misc.updateActions
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
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(dice, delta)),
          sounds = mapTableValues(deck.sounds, updateSound(delta)),
          spirits = mapTable(deck.spirits, updateAiState(world, delta)),
          timersInt = updateIntTimers(events)(deck.timersInt),
          timersFloat = mapTableValues(deck.timersFloat, updateFloatTimer(delta))
      )
    }

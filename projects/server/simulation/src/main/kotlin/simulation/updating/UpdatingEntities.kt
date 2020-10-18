package simulation.updating

import simulation.accessorize.updateAccessory
import silentorb.mythic.aura.updateSound
import silentorb.mythic.characters.rigs.updateCharacterRig
import silentorb.mythic.characters.rigs.updateThirdPersonCamera
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.mapTableValues
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.updateFloatCycle
import silentorb.mythic.timing.updateFloatTimer
import simulation.characters.updateCharacter
import simulation.combat.general.updateDestructibleHealth
import simulation.entities.updateAmbientAudio
import simulation.entities.updateAttachment
import simulation.entities.updateCharacterAnimation
import simulation.happenings.updateActions
import simulation.intellect.assessment.lightRatings
import simulation.intellect.assessment.updateKnowledge
import simulation.intellect.navigation.NavigationState
import simulation.intellect.navigation.updateNavigationDirections
import simulation.intellect.updateSpirit
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import simulation.movement.getFreedomTable
import simulation.particles.updateParticleEffect
import simulation.physics.CollisionGroups
import simulation.physics.updateBodies

fun updateEntities(definitions: Definitions, world: World, navigation: NavigationState?, events: Events): (Deck) -> Deck =
    { deck ->
      val delta = simulationDelta
      val dice = world.dice
      val freedomTable = getFreedomTable(deck)
      deck.copy(
          actions = updateActions(world.definitions, deck, events),
          ambientSounds = updateAmbientAudio(definitions, dice, deck),
          animations = mapTable(deck.animations, updateCharacterAnimation(deck, definitions.animations, delta)),
          bodies = mapTable(deck.bodies, updateBodies(world.realm.grid, deck, events, delta)),
          characterRigs = mapTable(deck.characterRigs, updateCharacterRig(world.bulletState, CollisionGroups.walkable, deck, freedomTable, events, delta)),
          accessories = mapTable(deck.accessories, updateAccessory(definitions, events)),
          attachments = mapTable(deck.attachments, updateAttachment(events)),
          cyclesFloat = mapTableValues(deck.cyclesFloat, updateFloatCycle(delta)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(events)),
          characters = mapTable(deck.characters, updateCharacter(definitions, dice, deck, world.bulletState, events)),
          knowledge = mapTable(deck.knowledge, updateKnowledge(world, lightRatings(world.deck), delta)),
          navigationDirections = if (navigation != null)updateNavigationDirections(navigation) else mapOf(),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(definitions.particleEffects, dice, delta)),
          sounds = mapTableValues(deck.sounds, updateSound(delta)),
          spirits = mapTable(deck.spirits, updateSpirit(world, delta)),
          thirdPersonRigs = mapTable(deck.thirdPersonRigs, updateThirdPersonCamera(world.bulletState.dynamicsWorld, CollisionGroups.affectsCamera, events, deck.bodies, deck.characterRigs, deck.targets, freedomTable, delta)),
          timersFloat = mapTableValues(deck.timersFloat, updateFloatTimer(delta))
      )
    }

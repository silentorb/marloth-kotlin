package simulation.updating

import silentorb.mythic.aura.updateSound
import silentorb.mythic.characters.rigs.updateCharacterRig
import silentorb.mythic.characters.rigs.updateThirdPersonCamera
import silentorb.mythic.ent.mapTable
import silentorb.mythic.ent.mapTableValues
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.updateInheritedBodyTransforms
import silentorb.mythic.timing.updateFloatCycle
import silentorb.mythic.timing.updateFloatTimer
import simulation.accessorize.updateAccessory
import simulation.characters.updateCharacter
import simulation.combat.general.updateDestructibleHealth
import simulation.entities.*
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
      val commands = events.filterIsInstance<Command>()
      deck.copy(
          actions = updateActions(world.definitions, deck, events),
          ambientSounds = updateAmbientAudio(definitions, dice, deck),
          animations = mapTable(deck.animations, updateCharacterAnimation(deck, definitions.animations, delta)),
          bodies = updateInheritedBodyTransforms(mapTable(deck.bodies, updateBodies(deck, events, delta))),
          characterRigs = mapTable(deck.characterRigs, updateCharacterRig(world.bulletState, CollisionGroups.static, deck, freedomTable, events, delta)),
          contracts = updateContracts(commands, deck.contracts),
          accessories = mapTable(deck.accessories, updateAccessory(definitions, events)),
          cyclesFloat = mapTableValues(deck.cyclesFloat, updateFloatCycle(delta)),
          destructibles = mapTable(deck.destructibles, updateDestructibleHealth(definitions, deck, events)),
          characters = mapTable(deck.characters, updateCharacter(definitions, dice, deck, world.bulletState, events)),
          knowledge = mapTable(deck.knowledge, updateKnowledge(world, lightRatings(world.deck), delta)),
          navigationDirections = if (navigation != null) updateNavigationDirections(navigation) else mapOf(),
          particleEffects = mapTableValues(deck.particleEffects, deck.bodies, updateParticleEffect(definitions.particleEffects, dice, delta)),
          players = mapTable(deck.players, updatePlayer(events)),
          primaryModes = mapTable(deck.primaryModes, updatePrimaryModes(commands)),
          sounds = mapTableValues(deck.sounds, updateSound(delta)),
          spirits = mapTable(deck.spirits, updateSpirit(world, delta)),
          thirdPersonRigs = mapTable(deck.thirdPersonRigs, updateThirdPersonCamera(world.bulletState.dynamicsWorld, CollisionGroups.affectsCamera, events, deck.bodies, deck.characterRigs, deck.targets, freedomTable, delta)),
          timersFloat = mapTableValues(deck.timersFloat, updateFloatTimer(delta))
      )
    }

package simulation.happenings

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.characters.EquipmentSlot
import simulation.main.Deck
import simulation.misc.Definitions

fun characterCommandToEquipmentSlot(type: String): EquipmentSlot =
    when (type) {
      CharacterCommands.abilityAttack -> EquipmentSlot.attack
      CharacterCommands.abilityDefense -> EquipmentSlot.defense
      CharacterCommands.abilityMobility -> EquipmentSlot.mobility
      CharacterCommands.abilityUtility -> EquipmentSlot.utility
      else -> throw Error("Not supported")
    }

fun commandsToEvents(definitions: Definitions, deck: Deck, commands: Commands): Events =
    commands.mapNotNull { command ->
      val actor = command.target
      when (command.type) {
        CharacterCommands.abilityAttack,
        CharacterCommands.abilityUtility,
        CharacterCommands.abilityDefense,
        CharacterCommands.abilityMobility -> {
          val slot = characterCommandToEquipmentSlot(command.type)
          val action = getEquippedAction(definitions, deck.accessories, slot, actor)
          if (action != null) {
            TryActionEvent(
                actor = actor,
                action = action
            )
          } else
            null
        }
        else -> null
      }
    }

package simulation.changing

import commanding.CommandType
import haft.Commands
import simulation.*

fun playerAttack(player: Player, commands: Commands<CommandType>): NewMissile? {
  val character = player.character

  val offset = if (player.viewMode == ViewMode.topDown)
    joinInputVector(commands, playerAttackMap)
  else if (commands.any { it.type == CommandType.attack })
    player.character.facingVector
  else
    null

  if (offset != null) {
    val ability = character.abilities[0]
    if (canUse(character, ability)) {
      return characterAttack(character, ability, offset)
    }
  }

  return null
}

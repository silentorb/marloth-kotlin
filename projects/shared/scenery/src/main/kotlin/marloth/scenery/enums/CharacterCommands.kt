package marloth.scenery.enums

object CharacterRigCommands {
  const val lookLeft = "lookLeft"
  const val lookRight = "lookRight"
  const val lookUp = "lookUp"
  const val lookDown = "lookDown"

  const val moveUp = "moveUp"
  const val moveDown = "moveDown"
  const val moveLeft = "moveLeft"
  const val moveRight = "moveRight"

  const val switchView = "switchView"
}

object CharacterCommands {
  const val lookLeft = CharacterRigCommands.lookLeft
  const val lookRight = CharacterRigCommands.lookRight
  const val lookUp = CharacterRigCommands.lookUp
  const val lookDown = CharacterRigCommands.lookDown

  const val moveUp = CharacterRigCommands.moveUp
  const val moveDown = CharacterRigCommands.moveDown
  const val moveLeft = CharacterRigCommands.moveLeft
  const val moveRight = CharacterRigCommands.moveRight

  const val equipSlot0 = "equipSlot0"
  const val equipSlot1 = "equipSlot1"
  const val equipSlot2 = "equipSlot2"
  const val equipSlot3 = "equipSlot3"

  const val interactPrimary = "interactPrimary"
  const val stopInteracting = "stopInteracting"

  const val ability = "ability"

}

package intellect

import mythic.spatial.Vector3
import physics.Force
import simulation.*

enum class SpiritActionType {
  move
}

data class SpiritAction(
    val type: SpiritActionType,
    val offset: Vector3
)

data class SpiritUpdateResult(
    val state: SpiritState,
    val actions: List<SpiritAction> = listOf()
)

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun getVisibleEnemy(world: World, spirit: Spirit): Character? {
  val enemies = world.characters.filter { it.faction != spirit.character.faction }
  return enemies.firstOrNull { canSee(spirit.character, it) }
}

fun checkEnemySighting(world: World, spirit: Spirit): SpiritState? {
  val visibleEnemy = getVisibleEnemy(world, spirit)
  return if (visibleEnemy == null)
    null
  else
    SpiritState(
        mode = SpiritMode.attacking,
        target = visibleEnemy
    )
}

fun updateAttack(world: World, spirit: Spirit): SpiritUpdateResult {
  val visibleEnemy = getVisibleEnemy(world, spirit)
  return if (visibleEnemy == null)
    SpiritUpdateResult(SpiritState(mode = SpiritMode.idle))
  else
    SpiritUpdateResult(spirit.state)
}

fun updateAiState(world: World, spirit: Spirit): SpiritUpdateResult {
  val enemySightingState = checkEnemySighting(world, spirit)
  if (enemySightingState != null)
    return SpiritUpdateResult(enemySightingState)

  return when (spirit.state.mode) {
    SpiritMode.idle -> SpiritUpdateResult(setDestination(world, spirit))
    SpiritMode.moving -> moveSpirit(spirit)
    SpiritMode.attacking -> updateAttack(world, spirit)
  }
}

fun applySpiritAction(spirit: Spirit, action: SpiritAction, delta: Float): Force? {
  return when (action.type) {
    SpiritActionType.move -> movementForce(spirit, action, delta)
  }
}

data class CharacterResult(
    val forces: List<Force> = listOf(),
    val newMissile: NewMissile? = null
)

fun updateSpirit(world: World, spirit: Spirit, delta: Float): CharacterResult {
  val result = updateAiState(world, spirit)
  spirit.state = result.state
  val forces = result.actions.mapNotNull { applySpiritAction(spirit, it, delta) }
  return CharacterResult(forces = forces)
//  return tryAiAttack(spirit)
}
package junk_simulation


typealias Id = Long

enum class Element {
  ethereal,
  plant,
  robot
}

data class Resource(
    val type: Element,
    val amount: Int,
    val max: Int
)

enum class ActionType {
  attack,
  flux,
  heal_self,
  none,
  upkeep,
  poison,
  poisoned,
  sacrifice,
  summon
}

enum class AbilityTarget {
  creature,
  global,
  element,
  enemy,
  none
}

enum class Effects {
  mass_damage,
  missileAttack,
  none
}

enum class Triggers {
  die,
  none,
  update
}

data class AbilityType(
    val name: String,
    val cooldown: Int = 0,
    val action: ActionType = ActionType.none,
    val info: String,
    val target: AbilityTarget = AbilityTarget.none,
    val aiTarget: AbilityTarget = AbilityTarget.none,
    val selectable: Boolean = true,
    val effect: Effects = Effects.none,
    val cost: Map<Element, Int> = mapOf(),
    val trigger: Triggers = Triggers.none,
    val arguments: List<Any> = listOf()
)

data class Ability(
    val id: Id,
    val type: AbilityType,
    val level: Int,
    val cooldown: Int
)

enum class CharacterCategory {
  minion,
  monster,
  player
}

data class CharacterType(
    val name: String,
    val category: CharacterCategory,
    val elements: Map<Element, Int> = mapOf(),
    val abilities: Map<AbilityType, Int> = mapOf(),
    val level: Int = 1,
    val frequency: Int = 0
)

data class Character(
    val id: Id,
    val type: CharacterType,
    val resources: List<Resource>
)

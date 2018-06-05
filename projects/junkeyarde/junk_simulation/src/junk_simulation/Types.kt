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

data class Action(
    val name: String
)

enum class AbilityTarget {
  global,
  element,
  none
}

enum class Effects {
  missileAttack,
  none
}

enum class Triggers {
  none,
  update
}

data class AbilityType(
    val name: String,
    val cooldown: Int = 0,
    val action: Action,
    val info: String,
    val target: AbilityTarget,
    val aiTarget: AbilityTarget = AbilityTarget.none,
    val selectable: Boolean = true,
    val effect: Effects = Effects.none,
    val cost: Map<Element, Int>,
    val trigger: Triggers = Triggers.none
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

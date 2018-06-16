package junk_simulation

typealias Id = Long

enum class Element {
  ethereal,
  plant,
  robot
}

data class Resource(
    val element: Element,
    val value: Int,
    val max: Int
)

data class SimpleResource(
    val element: Element,
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
    val purchasable: Boolean = true,
    val effect: Effects = Effects.none,
    val usageCost: Map<Element, Int> = mapOf(),
    val purchaseCost: Int = 1,
    val trigger: Triggers = Triggers.none,
    val arguments: List<Any> = listOf(),
    val parent: AbilityType? = null
)

data class Ability(
    val id: Id,
    val type: AbilityType,
    val cooldown: Int
)

enum class CreatureCategory {
  ally,
  enemy,
  player
}

data class CreatureType(
    val name: String,
    val category: CreatureCategory,
    val elements: Map<Element, Int> = mapOf(),
    val abilities: List<AbilityType> = listOf(),
    val life: Int,
    val level: Int = 1,
    val frequency: Int = 0
)

data class Creature(
    val id: Id,
    val level: Int,
    val type: CreatureType,
    val life: Int,
//    val resources: List<Resource>,
    val abilities: List<Ability>
)

typealias  CreatureMap = Map<Id, Creature>

enum class AnimationType {
  missile
}

data class Action(
    val actor: Id,
    val ability: Id,
    val target: Id?
)

data class Animation(
    val type: AnimationType,
    val progress: Float,
    val action: Action,
    val delay: Float
)
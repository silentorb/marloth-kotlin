package simulation.intellect.execution

import silentorb.mythic.combat.general.AttackMethod
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.CommonCharacterCommands
import silentorb.mythic.physics.SimpleBody
import silentorb.mythic.spatial.Vector3
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.main.Deck
import simulation.main.World
import simulation.misc.getActiveAction

fun shouldMoveDirectlyToward(deck: Deck, target: SimpleBody, attacker: Id): Boolean {
  val attackerBody = deck.bodies[attacker]!!
  throw Error("Not implemented")
//  return !isInAttackRange(attackerBody, target.position, deck.characters[attacker]!!.abilities[0])
//      && attackerBody.nearestNode == target.nearestNode
}

fun getWeaponAttackMethod(world: World, character: Id): AttackMethod? {
  val deck = world.deck
  val action = getActiveAction(deck, character)
  val accessory = deck.accessories[action]!!
  return if (action != null) {
    val weapon = world.definitions.weapons[accessory.type]!!
    weapon.attackMethod
  } else
    null
}

fun aimWeapon(world: World, character: Id, baseOffset: Vector3): Vector3 {
  val attackMethod = getWeaponAttackMethod(world, character)
  return if (attackMethod == AttackMethod.lobbed) {
    val distance = baseOffset.length()
    baseOffset + Vector3(0f, 0f, 0.1f) * distance
  } else
    baseOffset
}

fun spiritAttack(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val attacker = character
  val target = knowledge.characters[pursuit.targetEnemy]
  return if (target != null) {
    val body = world.deck.bodies[attacker]!!
    val offset = aimWeapon(world, character, target.position - body.position)
    spiritNeedsFacing(world, character, offset, 0.05f) {
      listOf(CharacterCommand(CommonCharacterCommands.ability, attacker))
    }
  } else
    listOf()
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
const val spiritAttackRangeBuffer = 0.1f

//fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Action): Boolean =
//    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


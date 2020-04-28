package simulation.intellect.execution

import marloth.scenery.enums.CharacterCommands
import simulation.combat.general.AttackMethod
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Commands
import silentorb.mythic.physics.SimpleBody
import silentorb.mythic.spatial.Vector3
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.main.Deck
import simulation.main.World
import simulation.happenings.getActiveAction

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

fun spiritAttack(world: World, attacker: Id, knowledge: Knowledge, pursuit: Pursuit): Commands {
  val target = knowledge.characters[pursuit.targetEnemy]
  return if (target?.position != null) {
    val body = world.deck.bodies[attacker]!!
    if (target.position.distance(world.deck.bodies[pursuit.targetEnemy]!!.position) > 0.3f) {
      println("Warning, large discrepancy between AI attack target locations")
    }
    val offset = aimWeapon(world, attacker, target.position - body.position)
    spiritNeedsFacing(world, attacker, offset, 0.2f) {
      listOf(CharacterCommand(CharacterCommands.ability, attacker))
    }
  } else
    listOf()
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
const val spiritAttackRangeBuffer = 0.1f

//fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Action): Boolean =
//    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


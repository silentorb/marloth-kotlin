package simulation.intellect.execution

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.SimpleBody
import silentorb.mythic.spatial.Vector3
import simulation.characters.EquipmentSlot
import simulation.combat.general.AttackMethod
import simulation.happenings.TryActionEvent
import simulation.happenings.getEquippedAction
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.design.actionsForTarget
import simulation.main.Deck
import simulation.main.World

fun shouldMoveDirectlyToward(deck: Deck, target: SimpleBody, attacker: Id): Boolean {
  val attackerBody = deck.bodies[attacker]!!
  throw Error("Not implemented")
//  return !isInAttackRange(attackerBody, target.position, deck.silentorb.mythic.characters[attacker]!!.abilities[0])
//      && attackerBody.nearestNode == target.nearestNode
}

fun getWeaponAttackMethod(world: World, character: Id): AttackMethod? {
  val deck = world.deck
  val action = getEquippedAction(world.definitions, deck.accessories, EquipmentSlot.attack, character)
  val accessory = deck.accessories[action]!!
  return if (action != null) {
    val weapon = world.definitions.weapons[accessory.type]
    weapon?.attackMethod
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

fun spiritAttack(world: World, attacker: Id, knowledge: Knowledge, pursuit: Pursuit): Events {
  val target = knowledge.characters[pursuit.targetEnemy]
  return if (target?.position != null) {
    val body = world.deck.bodies[attacker]!!
    if (target.position.distance(world.deck.bodies[pursuit.targetEnemy]!!.position) > 0.3f) {
      println("Warning, large discrepancy between AI attack target locations")
    }
    val offset = aimWeapon(world, attacker, target.position - body.position)
    spiritNeedsFacing(world, attacker, offset, 0.2f) {
      val actions = actionsForTarget(world, attacker, target.id)
      if (actions.none())
        listOf()
      else
        listOf(TryActionEvent(
            actor = attacker,
            action = world.dice.takeOne(actions),
            target = target.id,
            targetLocation = target.position
        ))
    }
  } else
    listOf()
}

// This is to ensure that a spirit attack when a target is cleanly within range
// instead of along the knife's-edge-fringe of its weapon range.
const val spiritAttackRangeBuffer = 0.1f

//fun isInAttackRange(attackerBody: Body, target: Vector3, ability: Action): Boolean =
//    attackerBody.position.distance(target) <= ability.definition.range - spiritAttackRangeBuffer


package silentorb.mythic.combat.spatial

import marloth.scenery.enums.MeshId
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.spatial.Quaternion
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.happenings.NewHandEvent
import simulation.main.Hand

fun missileAttack(world: SpatialCombatWorld, event: AttackEvent): Events {
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = world.definitions.weapons[accessory]!!
  val (origin, vector) = getAttackerOriginAndFacing(world.deck, attacker)
  return listOf(
      NewMissileEvent(
          position = origin,
          orientation = Quaternion(),
          force = vector * 10f,
          damages = weapon.damages,
          attacker = attacker
      ),
      NewHandEvent(
          hand = Hand(
              body = Body(
                  position = origin,
                  velocity = vector * 10f
              ),
              dynamicBody = DynamicBody(
                  gravity = true,
                  mass = 1f,
                  resistance = 1f
              ),
              depiction = Depiction(
                  type = DepictionType.staticMesh,
                  mesh = MeshId.spikyBall.name
              ),
              missile = Missile(
                  damageRadius = weapon.damageRadius,
                  damageFalloff = weapon.damageFalloff,
                  damages = weapon.damages
              )
          )
      )
  )
}

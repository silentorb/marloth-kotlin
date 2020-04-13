package silentorb.mythic.combat.spatial

import marloth.scenery.enums.MeshId
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.general.WeaponDefinition
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Sphere
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.happenings.NewHandEvent
import simulation.happenings.Trigger
import simulation.main.Hand

fun missileAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition): Events {
  val (origin, vector) = getAttackerOriginAndFacing(world.deck, attacker, 0.6f)
  return listOf(
      NewHandEvent(
          hand = Hand(
              body = Body(
                  position = origin,
                  velocity = vector * 10f
              ),
              collisionShape = CollisionObject(
                shape = Sphere(0.3f)
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
              ),
              trigger = Trigger()
          )
      )
  )
}

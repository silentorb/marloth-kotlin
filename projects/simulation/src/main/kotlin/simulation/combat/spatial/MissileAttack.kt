package simulation.combat.spatial

import simulation.combat.general.WeaponDefinition
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.CollisionObject
import silentorb.mythic.physics.DynamicBody
import silentorb.mythic.scenery.Light
import silentorb.mythic.scenery.LightType
import silentorb.mythic.scenery.Sphere
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.timing.FloatTimer
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.happenings.Trigger
import simulation.main.NewHand
import simulation.physics.CollisionGroups

fun missileAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition, targetLocation: Vector3?, targetEntity: Id?): Events {
  val (origin, vector) = getAttackerOriginAndFacing(world.deck, attacker, targetLocation, targetEntity,0.8f)
  return listOf(
      NewHand(
          components = listOf(
              Body(
                  position = origin,
                  velocity = vector * weapon.velocity,
                  scale = Vector3(0.5f)
              ),
              DynamicBody(
                  gravity = false,
                  mass = 1f,
                  resistance = 1f
              ),
              CollisionObject(
                  shape = Sphere(0.5f),
                  groups = CollisionGroups.dynamic,
                  mask = CollisionGroups.standardMask,
                  isSolid = false,
              ),
              Depiction(
                  type = DepictionType.staticMesh,
                  mesh = weapon.missileMesh
              ),
              Light(
                  type = LightType.point,
                  color = Vector4(1f, 0.9f, 0.6f, 1f),
                  offset = Vector3.zero,
                  range = 7f
              ),
              Missile(
                  damageRadius = weapon.damageRadius,
                  damageFalloff = weapon.damageFalloff,
                  damages = weapon.damages
              ),
              FloatTimer(1.5f),
              Trigger()
          )
      )
  )
}

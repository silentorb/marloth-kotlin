package simulation.physics

object CollisionGroups {
  const val static = 1
  const val dynamic = 2
  const val trigger = 4
  const val affectsCamera = 8
  const val walkable = 16

  const val tangibleMask = CollisionGroups.dynamic or CollisionGroups.static

  const val standardMask = CollisionGroups.dynamic or CollisionGroups.static or CollisionGroups.trigger

  const val staticMask = CollisionGroups.trigger or CollisionGroups.dynamic
}

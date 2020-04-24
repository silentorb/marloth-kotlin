package simulation.physics

object CollisionGroups {
  const val static = 1
  const val dynamic = 2
  const val trigger = 4
  const val affectsCamera = 8
  const val walkable = 16

  const val tangibleMask = dynamic or static

  const val standardMask = dynamic or static or trigger

  const val staticMask = dynamic or trigger
}

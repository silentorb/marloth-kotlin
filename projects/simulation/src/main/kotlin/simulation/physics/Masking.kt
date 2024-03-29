package simulation.physics

object CollisionGroups {
  val none = 0
  val static = 1
  val dynamic = 2
  val trigger = 4
  val affectsCamera = 8
  val opaque = 16
  val spirit = 32

  val solidStatic = static or opaque

  val solidStaticCamera = static or opaque or affectsCamera

  val tangibleMask = dynamic or static

  val standardMask = dynamic or static or trigger

  val staticMask = dynamic or trigger or spirit

  val spiritMask = spirit or static
}

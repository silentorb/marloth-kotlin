package org.joml

import mythic.spatial.Vector2

interface Vector2fMinimal {
  var x: Float
  var y: Float

  operator fun minus(v: Vector2fMinimal): Vector2

  fun xy(): Vector2
}

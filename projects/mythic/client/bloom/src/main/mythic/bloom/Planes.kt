package mythic.bloom

import org.joml.Vector2i

interface Plane {
  fun x(value: Vector2i): Int
  fun y(value: Vector2i): Int

  fun vector(first: Int, second: Int): Vector2i
  fun vector(value: Vector2i): Vector2i
}

class HorizontalPlane : Plane {
  override fun x(value: Vector2i) = value.x
  override fun y(value: Vector2i) = value.y

  override fun vector(first: Int, second: Int): Vector2i = Vector2i(first, second)

  override fun vector(value: Vector2i): Vector2i = value
}

class VerticalPlane : Plane {
  override fun x(value: Vector2i) = value.y
  override fun y(value: Vector2i) = value.x

  override fun vector(first: Int, second: Int): Vector2i = Vector2i(second, first)
  override fun vector(value: Vector2i): Vector2i = Vector2i(value.y, value.x)
}

val horizontalPlane = HorizontalPlane()
val verticalPlane = VerticalPlane()

package randomly

import mythic.spatial.Vector2
import java.util.Random

class Dice(val seed: Long) {
  val random = Random(seed)

  constructor() : this(System.currentTimeMillis())

  fun getFloat() = random.nextFloat()

  fun getFloat(min: Float, max: Float) = min + getFloat() * (max - min)

  fun getFloat(max: Float) = getFloat(0f, max)

  fun get(max: Vector2) = Vector2(getFloat(max.x), getFloat(max.y))

  fun <T> getItem(list: List<T>) = list[random.nextInt(list.size)]
}
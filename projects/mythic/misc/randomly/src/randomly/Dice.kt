package randomly

import mythic.spatial.Vector2
import java.util.Random

class Dice(private val seed: Long) {
  private val random = Random(seed)

  constructor() : this(System.currentTimeMillis())

  fun getFloat() = random.nextFloat()

  fun getFloat(min: Float, max: Float) = min + getFloat() * (max - min)

  fun getFloat(max: Float) = getFloat(0f, max)

  fun get(max: Vector2) = Vector2(getFloat(max.x), getFloat(max.y))

  fun <T> getItem(list: List<T>) = list[random.nextInt(list.size)]

  fun <T> getList(list: List<T>, count: Int): List<T> {
    assert(count <= list.size)
    val result = mutableListOf<T>()
    val options = list.toMutableList()
    for (i in 1..count) {
      val item = getItem(options)
      options.remove(item)
      result.add(item)
    }
    return result
  }

  companion object {
    val global = Dice()
  }
}
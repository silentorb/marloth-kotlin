package mythic.bloom.latest

import mythic.bloom.Bounds
import mythic.bloom.Depiction
import mythic.bloom.LogicModule
import mythic.bloom.StateBag
import org.joml.Vector2i

data class Box(
    val bounds: Bounds,
    val boxes: List<Box>,
    val depiction: Depiction? = null,
    val clipBounds: Bounds? = null,
    val handler: Any? = null,
    val logic: LogicModule? = null
)

data class Seed(
    val bag: StateBag = mapOf(),
    val dimensions: Vector2i,
    val clipBounds: Bounds? = null
)

typealias Flower = (Seed) -> Box

typealias LayoutDecorator = (Seed, Bounds) -> Bounds

typealias FlowerWrapper = (Flower) -> Flower

fun div(layoutDecorators: List<LayoutDecorator>): FlowerWrapper = { flower ->
  { seed ->
    val initialBounds = Bounds(
        position = Vector2i(),
        dimensions = seed.dimensions
    )
    val bounds = layoutDecorators.fold(initialBounds) { a, b -> b(seed, a) }
    val childSeed = seed.copy(
        dimensions = bounds.dimensions
    )
    Box(
        bounds = bounds,
        boxes = listOf(flower(childSeed))
    )
  }
}
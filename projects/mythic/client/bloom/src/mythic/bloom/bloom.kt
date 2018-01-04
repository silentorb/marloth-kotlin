package mythic.bloom

import mythic.spatial.Vector2

enum class Measurements {
  stretch,
  //  shrink,
  percent,
  pixel
}

data class Measurement(
    val type: Measurements,
    val value: Float
)
//
//data class AxisMeasurement(
//    val near: Measurement,
//    val length: Measurement,
//    val far: Measurement
//)

//data class Bounds(
//    val x: AxisMeasurement,
//    val y: AxisMeasurement
//)
//
//enum class BoxType {
//
//}
//
//data class Box(
//    val bounds: Bounds,
////    val type: BoxType,
//    val parent: Box? = null
//)

data class Bounds(
    val position: Vector2,
    val dimensions: Vector2
) {
  constructor(x: Float, y: Float, width: Float, height: Float) :
      this(Vector2(x, y), Vector2(width, height))
}

data class Box(
    val bounds: Bounds
) {
  constructor(x: Float, y: Float, width: Float, height: Float) :
      this(Bounds(x, y, width, height))
}

fun resolveMeasurement(measurement: Measurement, bound: Float) =
    when (measurement.type) {
      Measurements.pixel -> measurement.value
      Measurements.percent -> measurement.value * bound / 100
//      Measurements.shrink -> null
      Measurements.stretch -> null
    }

fun solveMeasurements(lengths: List<Measurement>, bound: Float): List<Float> {
  val resolved = lengths.map { resolveMeasurement(it, bound) }
  val exacts = resolved.filterNotNull()

  val total = exacts.fold(0f, { a, b -> a + b })
  if (exacts.size == lengths.size) {
    if (total != bound)
      throw Error("Could not stretch or shrink to fit bounds")

    return exacts
  } else {
    val stretchCount = lengths.size - exacts.size
    val stretchLength = (bound - total) / stretchCount
    return resolved.map { if (it != null) it else stretchLength }
  }
}

fun createVerticalPanels(widths: List<Measurement>, bounds: Vector2): List<Box> {
  var x = 0f
  return solveMeasurements(widths, bounds.x).map {
    val box = Box(x, 0f, it, bounds.y)
    x += it
    box
  }
}
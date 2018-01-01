package mythic.bloom

enum class Measurements {
  stretch,
  shrink,
  percent
}

data class Measurement(
    val type: Measurements,
    val value: Float
)

data class AxisMeasurement(
    val near: Measurement,
    val length: Measurement,
    val far: Measurement
)

data class Bounds(
    val x: AxisMeasurement,
    val y: AxisMeasurement
)

enum class BoxType {

}

data class Box(
    val parent: Box?,
    val type: BoxType,
    val bounds: Bounds
)
package simulation.main

const val updateFrequency = 2

fun divideUp(dividend: Int, divisor: Int): Int {
  val result = dividend / divisor
  return if (divisor * result != dividend)
    result + 1
  else
    result
}


val overTime: (Int) -> Int = { value ->
  divideUp(value, updateFrequency)
}

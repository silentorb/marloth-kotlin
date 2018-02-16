package simulation

class Resource(
    var max: Int
) {
  var value = max

  fun modify(mod: Int) {
    val newValue = mod + value
    if (newValue < 0)
      value = 0
    else if (newValue > max)
      value = max
    else
      value = newValue
  }
}
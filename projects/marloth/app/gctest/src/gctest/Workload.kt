package gctest

data class A(
    val value: Int
)

fun getResult(a: A) =
    a.value + 1

class Workload {
  var unusedResult = 0

  fun update() {
    var result = 0
    for (i in 0 until 1000) {
      val a = A(i)
      result += getResult(a)
    }

    unusedResult = 0
  }
}
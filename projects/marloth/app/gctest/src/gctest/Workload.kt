package gctest

data class A(
    val value: Int,
    val t1: Long = 10,
    val t2: Long = 20,
    val t3: Long = 20,
    val t4: Long = 20,
    val t5: Long = 20,
    val t6: Long = 20
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
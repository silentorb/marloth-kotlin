package gctest

const val OneMillion = 1000000

const val objectCount = 100000

data class A(
    var value: Int,
    var second: Int,
    var third: Int,
    var b: B? = null
)

fun getResult(a: A) =
    a.value + a.second + a.third + 1

fun createA(i: Int): A {
  return A(i, 0, i)
}

data class B(
    var a: A?
)

class Workload(val useBList: Boolean) {
  var unusedResult = 0
  var blist: List<B> = if (useBList)
    (0 until objectCount * 100).map { B(A(10, 0, 0)) }
  else
    listOf()

  fun update(creator: (Int) -> A) {
//    println("Updating")
    var result = 0
    for (i in 0 until objectCount) {
      val a = creator(i)
      if (useBList) {
        a.b = blist[i]
      }
      result += getResult(a)
    }

    unusedResult = result
  }
}
package marloth.clienting.gui.menus.general

import silentorb.mythic.ent.copyDataClass
import silentorb.mythic.ent.getPropertyValue

tailrec fun getOptionValue(options: Any, path: List<String>): Any? {
  assert(path.any())
  return if (path.size == 1)
    getPropertyValue(options, path.first())
  else {
    val parent = getPropertyValue<Any>(options, path.first())
    if (parent == null)
      null
    else
      getOptionValue(parent, path.drop(1))
  }
}

fun getOptionValue(options: Any, path: String): Any? =
    getOptionValue(options, path.split('.'))

fun setOptionValue(options: Any, path: List<String>, value: Any): Any? {
  assert(path.any())
  return if (path.size == 1)
    copyDataClass(options, mapOf(path.first() to value))
  else {
    val property = path.first()
    val child = getPropertyValue<Any>(options, property)
    if (child != null) {
      val nextChild = setOptionValue(child, path.drop(1), value)
      if (nextChild != null) {
        val k = copyDataClass(options, mapOf(property to nextChild))
        k
      } else
        null
    } else
      null
  }
}

fun setOptionValue(options: Any, path: String, value: Any): Any? =
    setOptionValue(options, path.split('.'), value)

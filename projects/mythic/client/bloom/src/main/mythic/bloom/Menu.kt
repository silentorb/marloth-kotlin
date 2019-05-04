package mythic.bloom

import mythic.bloom.next.Flower
import mythic.bloom.next.margin
import mythic.typography.IndexedTextStyle

data class MenuItem(
    val name: String
)

data class Menu(
    val name: String,
    val character: String,
    val items: List<MenuItem>
)

data class GlobalMenuState(
    val activeMenu: String?
)

val globalMenuState = existingOrNewState {
  GlobalMenuState(
      activeMenu = null
  )
}

fun menuBar(key: String, style: IndexedTextStyle, menus: List<Menu>): Flower {
  val bar = list(horizontalPlane, 10)
  val items = menus.map { menu ->
    label(style, menu.name)
  }

  return margin(all = 10)(bar(items))

//  return { seed ->
//    val globalState = globalMenuState(seed.bag[key])
//    val menuItems = menus.map {
//
//    }
//    bar(menus)
//  }
}

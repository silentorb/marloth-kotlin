package mythic.bloom

import mythic.bloom.next.*
import mythic.spatial.Vector4
import mythic.typography.IndexedTextStyle
import org.joml.minus

data class MenuItem(
    val name: String
)

data class Menu(
    val name: String,
    val character: String,
    val items: List<MenuItem>
)

//data class GlobalMenuState(
//    val activeMenu: String?
//)
//
//private val globalMenuState = existingOrNewState {
//  GlobalMenuState(
//      activeMenu = null
//  )
//}

private val globalMenuSelectionKey = "menuSelection"
private val selectableMenu = selectable<Menu>(globalMenuSelectionKey, optionalSingleSelection) {
  it.hashCode().toString()
}

private val invertColor = { color: Vector4 ->
  Vector4(1f - color.x, 1f - color.y, 1f - color.z, 1f)
}

fun menuBar(style: IndexedTextStyle, menus: List<Menu>): Flower {
  val bar = list(horizontalPlane, 10)
  val items = menus.map { menu ->
    val depiction = selectableFlower(globalMenuSelectionKey, menu.hashCode().toString()) { seed, selected ->
      if (selected) {
        val newStyle = style.copy(
            color = invertColor(style.color)
//            color = Vector4(1f, 0f, 0f, 1f)
        )
//        div(reverse = shrink)(depict(solidBackground(style.color)) plusFlower label(newStyle, menu.name))
        div(reverse = shrink)(margin(all = 10)(label(newStyle, menu.name)) depictBehind solidBackground(style.color))
//        depict(solidBackground(style.color)) plusFlower
//        label(newStyle, menu.name)
      } else
        label(style, menu.name)
//        margin(all = 10)(label(style, menu.name))
    }
    depiction plusLogic selectableMenu(menu)
  }

  return bar(items)

//  return { seed ->
//    val globalState = globalMenuState(seed.bag[key])
//    val menuItems = menus.map {
//
//    }
//    bar(menus)
//  }
}

package marloth.clienting.gui.menus.general.forms

import marloth.clienting.AppOptions
import marloth.clienting.ClientEventType
import silentorb.mythic.bloom.*
import marloth.clienting.gui.Colors
import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.MenuItemFlower
import marloth.clienting.gui.menus.general.getOptionValue
import marloth.clienting.gui.menus.logic.onActivateKey
import silentorb.mythic.happenings.Command
import silentorb.mythic.spatial.Vector2i

const val checkboxIdKey = "silentorb.bloom.checkbox"

fun checkboxDepiction(value: Boolean): Depiction = { bounds, canvas ->
  drawBorder(bounds, canvas, Colors.black, 2f)
  if (value) {
    val gap = 6
    val boxBounds = Bounds(
        dimensions = bounds.dimensions - gap * 2,
        position = bounds.position + gap
    )
    drawFill(boxBounds, canvas, Colors.black)
  }
}

//fun checkboxFlower(id: Any, value: Boolean): Flower =
//    div(
//        forward = forwardDimensions(fixed(50), fixed(50)),
//        attributes = mapOf(checkboxIdKey to id)
//    )(depict(checkboxDepiction(value)))
//
//fun getToggledCheckbox(hoverBoxes: Collection<OffsetBox>): Any? =
//    getAttributeValue(hoverBoxes, checkboxIdKey)

fun checkboxFlower(handlers: List<Any>, value: Boolean): MenuItemFlower = { hasFocus ->
  val box = depictBox(Vector2i(32, 32), checkboxDepiction(value))

  if (hasFocus)
    box.addAttributes(onActivateKey to handlers)
  else
    box
}

fun checkboxFlowerField(label: String, handlers: List<Any>, value: Boolean): MenuItem =
    MenuItem(
        flower = menuField(label, checkboxFlower(handlers, value)),
        events = handlers,
    )

fun toggleOptionField(options: AppOptions, label: String, path: String): MenuItem {
  val value = getOptionValue(options, path) as? Boolean ?: false
  return checkboxFlowerField(label, listOf(Command(ClientEventType.setOption, !value, path)), value)
}

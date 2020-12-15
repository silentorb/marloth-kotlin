package marloth.clienting.gui.menus.general.forms

import silentorb.mythic.bloom.*
import marloth.clienting.gui.Colors

const val checkboxIdKey = "silentorb.bloom.checkbox"

fun checkboxDepiction(value: Boolean): Depiction = { bounds, canvas ->
  drawBorder(bounds, canvas, Colors.black)
  if (value) {
    val gap = 5
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

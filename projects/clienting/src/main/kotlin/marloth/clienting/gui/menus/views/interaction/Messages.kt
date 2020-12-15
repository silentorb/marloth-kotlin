package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialog
import marloth.clienting.gui.menus.dialogWrapper
import marloth.scenery.enums.DevText
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.label

fun messageDialog(message: Text): StateFlowerTransform = dialogWrapper { definitions, state ->
  dialog(definitions, DevText("Message"),
      label(TextStyles.smallBlack, definitions.textLibrary(message))
  )
}

val messageTooSoon = messageDialog(DevText("You couldn't be done yet."))

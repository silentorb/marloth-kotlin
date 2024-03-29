package marloth.clienting.gui.hud

import marloth.clienting.gui.menus.black
import silentorb.mythic.bloom.*

fun hudBox(box: Box): Box =
    boxMargin(10)(
        boxMargin(20)(
            box
        )
            .copy(depiction = solidBackground(black))
    )

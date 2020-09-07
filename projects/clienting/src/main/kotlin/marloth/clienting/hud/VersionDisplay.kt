package marloth.clienting.hud

import marloth.clienting.menus.black
import marloth.clienting.menus.textStyles
import silentorb.mythic.bloom.*

fun versionDisplay(version: String): Flower =
    div(reverse = reverseOffset(top = justifiedEnd, left= justifiedEnd))(
        margin(10)(
            div(reverse = shrink, depiction = solidBackground(black))(
                margin(20)(
                    label(textStyles.smallWhite, "Version $version")
                )
            )
        )
    )

package marloth.clienting.hud

import marloth.clienting.menus.black
import marloth.clienting.menus.TextStyles
import silentorb.mythic.bloom.*

fun versionDisplay(version: String): Flower =
    div(reverse = reverseOffset(top = justifiedEnd, left = justifiedEnd))(
        forwardMargin(10)(
            div(reverse = shrink, depiction = solidBackground(black))(
                boxToFlower(
                    reverseMargin(20)(
                        label(TextStyles.smallWhite, "Version $version")
                    )
                )
            )
        )
    )

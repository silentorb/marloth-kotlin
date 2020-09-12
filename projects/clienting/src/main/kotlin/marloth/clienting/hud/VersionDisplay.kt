package marloth.clienting.hud

import marloth.clienting.menus.black
import marloth.clienting.menus.TextStyles
import silentorb.mythic.bloom.*

fun versionDisplay(version: String): Flower =
    reverseOffset(justifiedEnd)(
        hudBox(
            label(TextStyles.smallWhite, "Version $version")
        )
    )

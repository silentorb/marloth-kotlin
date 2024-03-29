package marloth.clienting.gui.hud

import marloth.clienting.gui.menus.TextStyles
import silentorb.mythic.bloom.*

fun versionDisplay(version: String): Flower =
    alignBoth(justifiedEnd,
        hudBox(
            label(TextStyles.smallWhite, "Version $version")
        )
    )

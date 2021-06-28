package marloth.clienting.gui.menus

import marloth.clienting.resources.UiTextures
import silentorb.mythic.bloom.*
import silentorb.mythic.spatial.Vector2i

fun silentOrbDepiction() =
    Box(dimensions = Vector2i(128, 64), depiction = imageDepiction(UiTextures.silentorb))

fun silentOrbDisplay(): Flower =
    alignBoth(centered, justifiedEnd,
        boxMargin(30)(
            silentOrbDepiction()
        )
    )

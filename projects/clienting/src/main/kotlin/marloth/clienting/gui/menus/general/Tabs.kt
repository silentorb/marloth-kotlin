package marloth.clienting.gui.menus.general

import marloth.clienting.ClientEventType
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.general.forms.cycle
import marloth.clienting.gui.menus.logic.cycle
import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.*
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands

data class Tab(
    val view: ViewId,
    val title: Text,
)

fun updateTabNavigation(tabs: List<Tab>, view: ViewId, commands: Commands): Commands {
  val nextView = when {
    commands.any { it.type == GuiCommandType.tabNext } -> cycle(tabs.map { it.view }, view, 1)
    commands.any { it.type == GuiCommandType.tabPrevious } -> cycle(tabs.map { it.view }, view, -1)
    else -> null
  }

  return if (nextView != null)
    listOf(Command(ClientEventType.navigate, nextView))
  else
    listOf()
}

fun tabFlower(textLibrary: TextResourceMapper, tab: Tab, active: Boolean): Box {
  val box = boxMargin(10)(label(TextStyles.mediumBlack, textLibrary(tab.title))) handle onClick {
    listOf(Command(ClientEventType.navigate, tab.view))
  }
  return if (active)
    box depictBehind drawMenuButtonBackground(false)
  else
    box
}

fun tabView(textLibrary: TextResourceMapper, tabs: List<Tab>, view: ViewId): Box {
  return horizontalList(10)(
      tabs.map { tab ->
        tabFlower(textLibrary, tab, view == tab.view)
      }
  )
      .handle { input, _ ->
        updateTabNavigation(tabs, view, input.commands)
      }
}

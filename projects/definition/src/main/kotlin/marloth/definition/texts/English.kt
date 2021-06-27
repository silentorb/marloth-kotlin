package marloth.definition.texts

import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import marloth.scenery.enums.TextResourceMapper

val englishTextResources: TextResourceMapper = { text ->
  when (text) {
    is DevText -> text.value
    is TextId ->
      when (text) {
        TextId.damageType_cold -> "Cold"
        TextId.damageType_fire -> "Fire"
        TextId.damageType_poison -> "Poison"

        // Options Menus
        TextId.gui_optionsMenu -> "Options"
        TextId.gui_audioOptions -> "Audio"
        TextId.gui_displayOptions -> "Display"
        TextId.gui_inputOptions -> "Input"
        TextId.gui_inputBindings -> "Input Bindings"
        TextId.gui_gamepadOptions -> "Gamepad"
        TextId.gui_mouseOptions -> "Mouse"

        // Display Options
        TextId.gui_antialiasing -> "Antialiasing"
        TextId.gui_fullscreen -> "Fullscreen"
        TextId.gui_resolution -> "Resolution"
        TextId.gui_vsync -> "VSync"
        TextId.gui_windowed -> "Windowed"
        TextId.gui_windowedFullscreen -> "Windowed Fullscreen"
        TextId.gui_windowMode -> "Window Mode"
        TextId.gui_query_saveDisplayChanges -> "Save Display Changes?"

        TextId.gui_yes -> "Yes"
        TextId.gui_no -> "No"

        TextId.gui_accessories -> "Accessories"
        TextId.gui_characterInfo -> "Character Info"
        TextId.gui_chooseAccessoryMenu -> "Accessorize"
        TextId.gui_chooseProfessionMenu -> "Profession"
        TextId.gui_merchant -> "Merchant"
        TextId.gui_mainMenu -> "Menu"
        TextId.gui_manual -> "Manual"
        TextId.gui_profession -> "Profession"
        TextId.gui_resistances -> "Resistances"
        TextId.gui_take -> "Take"
        TextId.gui_victory -> "Victory"
        TextId.id_candle -> "Candle"
        TextId.id_damageChilled -> "Chilled"
        TextId.id_damageBurning -> "Burning"
        TextId.id_damagePoisoned -> "Poisoned"
        TextId.id_dash -> "Dash"
        TextId.id_entangle -> "Entangle"
        TextId.id_entangled -> "Entangled"
        TextId.id_graveDigger -> "Grave Digger"
        TextId.id_graveDiggerDescription -> "Slain foes take longer to respawn"
        TextId.id_grenadeLauncher -> "Grenade Launcher"
        TextId.id_magician -> "Magician"
        TextId.id_mobility -> "Mobility"
        TextId.id_mobile -> "Mobile"
        TextId.gui_money -> "Money"
        TextId.id_pistol -> "Pistol"
        TextId.id_resistanceCold -> "Cold Resistance"
        TextId.id_resistanceFire -> "Fire Resistance"
        TextId.id_resistancePoison -> "Poison Resistance"
        TextId.id_rocketLauncher -> "Rocket Launcher"
        TextId.id_soldier -> "Soldier"
        TextId.menu_close -> "Close"
        TextId.menu_continueGame -> "Continue"
        TextId.menu_newGame -> "New Game"
        TextId.menu_open -> "Open"
        TextId.menu_talk -> "Talk"
        TextId.menu_quit -> "Quit"
        TextId.message_victory -> "You Won!"
        TextId.unnamed -> "Unnamed"
      }
    else -> throw Error("Not supported")
  }
}

package marloth.clienting.textResources

import marloth.clienting.TextResourceMapper
import marloth.scenery.enums.Text

val englishTextResources: TextResourceMapper = { text ->
  when (text) {
    Text.damageType_cold -> "Cold"
    Text.damageType_fire -> "Fire"
    Text.damageType_poison -> "Poison"
    Text.gui_characterInfo -> "Character Info"
    Text.gui_merchant -> "Merchant"
    Text.gui_mainMenu -> "Menu"
    Text.gui_resistances -> "Resistances"
    Text.gui_take -> "Take"
    Text.gui_victory -> "Victory"
    Text.id_candle -> "Candle"
    Text.id_damageChilled -> "Chilled"
    Text.id_damageBurning -> "Burning"
    Text.id_damagePoisoned -> "Poisoned"
    Text.id_grenadeLauncher -> "Grenade Launcher"
    Text.id_pistol -> "Pistol"
    Text.id_resistanceCold -> "Cold Resistance"
    Text.id_resistanceFire -> "Fire Resistance"
    Text.id_resistancePoison -> "Poison Resistance"
    Text.id_rocketLauncher -> "Rocket Launcher"
    Text.menu_close -> "Close"
    Text.menu_continueGame -> "Continue"
    Text.menu_newGame -> "New Game"
    Text.menu_open -> "Open"
    Text.menu_talk -> "Talk"
    Text.menu_quit -> "Quit"
    Text.message_victory -> "You Won!"
  }
}

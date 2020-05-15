package marloth.definition.texts

import marloth.scenery.enums.Text
import marloth.scenery.enums.TextResourceMapper

val englishTextResources: TextResourceMapper = { text ->
  when (text) {
    Text.damageType_cold -> "Cold"
    Text.damageType_fire -> "Fire"
    Text.damageType_poison -> "Poison"
    Text.gui_accessories -> "Modifiers"
    Text.gui_characterInfo -> "Character Info"
    Text.gui_chooseAccessoryMenu -> "Accessorize"
    Text.gui_chooseProfessionMenu -> "Profession"
    Text.gui_merchant -> "Merchant"
    Text.gui_mainMenu -> "Menu"
    Text.gui_profession -> "Profession"
    Text.gui_resistances -> "Resistances"
    Text.gui_take -> "Take"
    Text.gui_victory -> "Victory"
    Text.id_candle -> "Candle"
    Text.id_damageChilled -> "Chilled"
    Text.id_damageBurning -> "Burning"
    Text.id_damagePoisoned -> "Poisoned"
    Text.id_dash -> "Dash"
    Text.id_entangle -> "Entangle"
    Text.id_entangled -> "Entangled"
    Text.id_graveDigger -> "Grave Digger"
    Text.id_graveDiggerDescription -> "Slain foes take longer to respawn"
    Text.id_grenadeLauncher -> "Grenade Launcher"
    Text.id_magician -> "Magician"
    Text.id_mobility -> "Mobility"
    Text.id_mobile -> "Mobile"
    Text.id_pistol -> "Pistol"
    Text.id_resistanceCold -> "Cold Resistance"
    Text.id_resistanceFire -> "Fire Resistance"
    Text.id_resistancePoison -> "Poison Resistance"
    Text.id_rocketLauncher -> "Rocket Launcher"
    Text.id_soldier -> "Soldier"
    Text.menu_close -> "Close"
    Text.menu_continueGame -> "Continue"
    Text.menu_newGame -> "New Game"
    Text.menu_open -> "Open"
    Text.menu_talk -> "Talk"
    Text.menu_quit -> "Quit"
    Text.message_victory -> "You Won!"
    Text.unnamed -> "Unnamed"
  }
}

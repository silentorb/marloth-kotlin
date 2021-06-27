package marloth.scenery.enums

interface Text

data class DevText(
    val value: String
) : Text

enum class TextId : Text {
  damageType_cold,
  damageType_fire,
  damageType_poison,

  gui_yes,
  gui_no,

  gui_accessories,
  gui_audioOptions,
  gui_characterInfo,
  gui_chooseAccessoryMenu,
  gui_chooseProfessionMenu,
  gui_displayOptions,
  gui_inputBindings,
  gui_inputOptions,
  gui_gamepadOptions,
  gui_merchant,
  gui_mainMenu,
  gui_manual,
  gui_money,
  gui_mouseOptions,
  gui_optionsMenu,
  gui_profession,
  gui_resistances,
  gui_take,
  gui_victory,

  // Display options
  gui_antialiasing,
  gui_fullscreen,
  gui_windowedFullscreen,
  gui_windowed,
  gui_windowMode,
  gui_resolution,
  gui_vsync,
  gui_query_saveDisplayChanges,

  id_candle,
  id_damageChilled,
  id_damageBurning,
  id_damagePoisoned,
  id_dash,
  id_entangle,
  id_entangled,
  id_graveDigger,
  id_graveDiggerDescription,
  id_grenadeLauncher,
  id_magician,
  id_mobile,
  id_mobility,
  id_resistanceCold,
  id_resistanceFire,
  id_resistancePoison,
  id_rocketLauncher,
  id_soldier,
  id_pistol,

  menu_close,
  menu_continueGame,
  menu_newGame,
  menu_open,
  menu_talk,
  menu_quit,
  message_victory,
  unnamed
}

typealias TextResourceMapper = (Text) -> String

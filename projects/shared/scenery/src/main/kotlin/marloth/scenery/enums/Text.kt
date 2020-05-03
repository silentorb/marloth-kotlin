package marloth.scenery.enums

enum class Text {
  damageType_cold,
  damageType_fire,
  damageType_poison,

  gui_characterInfo,
  gui_chooseProfessionMenu,
  gui_merchant,
  gui_mainMenu,
  gui_resistances,
  gui_take,
  gui_victory,

  id_candle,
  id_damageChilled,
  id_damageBurning,
  id_damagePoisoned,
  id_dash,
  id_entangle,
  id_entangled,
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

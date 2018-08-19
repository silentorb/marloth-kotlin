package marloth.clienting

enum class CommandType {
  none,

  lookLeft,
  lookRight,
  lookUp,
  lookDown,

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

  jump,
  attack,
  duck,
  run,

  switchView,

  activateDevice,
  joinGame,
  menu,
  menuSelect,
  menuBack,
}
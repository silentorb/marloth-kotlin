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

  newGame,

  switchView,

  activateDevice,
  joinGame,
  menu,
  menuSelect,
  menuBack,
}
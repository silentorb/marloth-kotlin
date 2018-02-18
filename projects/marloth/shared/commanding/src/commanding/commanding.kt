package commanding

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

//  attackUp,
//  attackDown,
//  attackLeft,
//  attackRight,
  
  jump,
  attack,
  duck,
  run,

  switchView,

  activateDevice,
  joinGame,
  menuBack,
  select,
}
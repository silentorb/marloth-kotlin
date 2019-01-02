package marloth.clienting.input

enum class GuiCommandType {
  none,

  moveUp,
  moveDown,
  moveLeft,
  moveRight,

  menu,
  menuSelect,
  menuBack,

  newGame,
  quit,

}

private fun standardClientStrokes() = setOf(
    GuiCommandType.menu,
    GuiCommandType.menuSelect,
    GuiCommandType.menuBack,
    GuiCommandType.newGame,
    GuiCommandType.quit
)

val clientCommandStrokes = mapOf(
    BindingContext.game to standardClientStrokes(),
    BindingContext.menu to standardClientStrokes()
        .plus(setOf(
            GuiCommandType.moveUp,
            GuiCommandType.moveDown,
            GuiCommandType.moveLeft,
            GuiCommandType.moveRight
        ))
)
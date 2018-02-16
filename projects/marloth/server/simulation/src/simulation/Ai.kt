package simulation

fun getAiPlayers(world: World) =
    world.characters.values.filter { isPlayer(world, it) }

fun updateEnemy(world: World, character: Character) {
  val shoot = character.abilities[0]
  val player = world.players.firstOrNull { it.character.body.position.distance(character.body.position) <= shoot.range }
}
package simulation

import mythic.ent.Entity
import mythic.ent.Id

enum class ViewMode {
  firstPerson,
  //  topDown
  thirdPerson
}

data class HoverCamera(
    var pitch: Float = -0.4f,
    var yaw: Float = 0f,
    var distance: Float = 7f
)

data class Player(
    override val id: Id,
    val playerId: Int,
    val name: String,
    val viewMode: ViewMode,
    val hoverCamera: HoverCamera = HoverCamera()
) : Entity

data class PlayerCharacter(
    val player: Player,
    val character: Character
)

typealias PlayerCharacters = List<PlayerCharacter>

fun isPlayer(deck: Deck, character: Character) =
    deck.players.any { it.key == character.id }

package marloth.clienting.gui

import mythic.ent.Id
import simulation.main.Deck

fun getPlayerInteractingWith(deck: Deck): Id? =
    deck.characters[deck.players.keys.first()]!!.interactingWith

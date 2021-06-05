package simulation.main

import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Commands
import simulation.intellect.navigation.NavigationState
import silentorb.mythic.randomly.Dice
import simulation.misc.Definitions
import silentorb.mythic.physics.BulletState

data class World(
    val nextId: SharedNextId,
    val deck: Deck,
    val global: GlobalState,
    val dice: Dice,
    val navigation: NavigationState?,
    val staticGraph: GraphWrapper = GraphWrapper(newGraph()),
    val bulletState: BulletState,
    val definitions: Definitions,
    val persistence: Graph,
    val step: Long,
    val nextCommands: Commands = listOf(),
)

typealias WorldPair = Pair<World, World>

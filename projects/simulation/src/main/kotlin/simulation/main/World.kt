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

// GeneratedWorld was mainly created to defer initialization of bullet in case it was having threading problems
// but that turned out not to be the case.
// I'm still leaving GeneratedWorld for now because there is some organizational benefit to separate
// the generation output from the additional runtime fields World needs
data class GeneratedWorld(
    val nextId: SharedNextId,
    val deck: Deck,
    val global: GlobalState,
    val navigation: NavigationState?,
    val graph: Graph,
    val persistence: Graph,
)

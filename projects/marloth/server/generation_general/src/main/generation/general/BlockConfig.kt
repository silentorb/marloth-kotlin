package generation.general

data class BlockConfig(
    val blocks: Set<Block>,
    val independentConnections: Set<Any>,
    val openConnections: Set<Any>,
    val isSideIndependent: SideCheck = generation.general.isSideIndependent(independentConnections)
)

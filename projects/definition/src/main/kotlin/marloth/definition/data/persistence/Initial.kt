package marloth.definition.data.persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.LooseGraph

fun initialHistoricalData(): LooseGraph = listOf(
    Entry("namelessCity", PersistenceProperties.leader, "kingBob")
)

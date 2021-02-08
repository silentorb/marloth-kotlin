package marloth.definition.data.persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph

fun initialHistoricalData(): Graph = listOf(
    Entry("namelessCity", PersistenceProperties.leader, "kingBob")
)

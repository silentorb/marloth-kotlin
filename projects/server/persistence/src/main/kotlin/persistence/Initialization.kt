package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.LooseGraph

object Rulers {
  val kingBob = "kingBob"
}

fun initialPersistentData(): LooseGraph = listOf(
    Entry("global", "ruler", Rulers.kingBob)
)

fun initializeDatabase(db: Database) {
  executeSql(db, schemaSql())
  insertEntries(db, initialPersistentData())
}

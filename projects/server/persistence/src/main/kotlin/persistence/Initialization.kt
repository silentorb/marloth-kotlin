package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.LooseGraph

object Rulers {
  val kingBob = "kingBob"
}

fun initialHistoricalData(): LooseGraph = listOf(
    Entry("global", "ruler", Rulers.kingBob)
)

fun initializeDatabaseInfoTable(db: Database) {
  val sql = "INSERT INTO database_info (format_version) VALUES (?)"
  executeSqlWithArguments(db, sql, listOf(databaseFormatVersion))
}

fun initializeDatabase(db: Database) {
  executeSql(db, schemaSql())
  insertEntries(db, initialHistoricalData())
  initializeDatabaseInfoTable(db)
}

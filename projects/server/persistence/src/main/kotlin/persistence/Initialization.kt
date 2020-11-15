package persistence

import silentorb.mythic.ent.LooseGraph

fun initializeDatabaseInfoTable(db: Database) {
  val sql = "INSERT INTO database_info (format_version) VALUES (?)"
  executeSqlWithArguments(db, sql, listOf(databaseFormatVersion))
}

fun initializeDatabase(db: Database, initialPersistence: LooseGraph) {
  for (tableSql in schemaSql()) {
    executeSql(db, tableSql)
  }

  insertEntries(db, "persistence", initialPersistence)
  initializeDatabaseInfoTable(db)
}

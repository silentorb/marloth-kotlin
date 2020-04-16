package persistence

fun initializeDatabase(db: Database) {
  val sql = """

CREATE TABLE victories (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  player STRING NOT NULL,
  created DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP)
);

  """
  executeSql(db, sql)
}
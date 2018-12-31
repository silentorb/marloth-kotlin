package persistence

import org.sqlite.SQLiteDataSource
import simulation.PlayerStats
import simulation.Victory
import java.io.File
import java.sql.ResultSet
import javax.sql.DataSource

data class Database(
    val source: DataSource
)

fun newDatabase(filepath: String): Database {
  val source = SQLiteDataSource()
  source.url = "jdbc:sqlite:$filepath"
  val db = Database(source)
  if (!File(filepath).exists())
    initializeDatabase(db)

  return db
}

fun <T> querySql(db: Database, sql: String, handler: (ResultSet) -> T): T {
  val connection = db.source.connection
  val statement = connection.createStatement()
  return handler(statement.executeQuery(sql))
}

fun executeSql(db: Database, sql: String) {
  val connection = db.source.connection
  val statement = connection.createStatement()
  statement.execute(sql)
}

fun createVictory(db: Database, victory: Victory) {
  val sql = """
    INSERT INTO victories (player) VALUES ('${victory.player}')
  """.trimIndent()
  executeSql(db, sql)
}

fun queryStats(db: Database, player: String): PlayerStats {
  val sql = """
    SELECT
      (SELECT COUNT(*) FROM victories WHERE player = $player) as victoryCount
  """.trimIndent()
  return querySql(db, sql) {
    PlayerStats(
        player = player,
        victoryCount = it.getInt(0)
    )
  }
}
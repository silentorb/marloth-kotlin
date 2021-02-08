package persistence

import org.sqlite.SQLiteDataSource
import silentorb.mythic.ent.Graph
import java.io.File
import javax.sql.DataSource

data class Database(
    val source: DataSource
)

fun newDatabase(filepath: String, initialPersistence: () -> Graph): Database {
  val source = SQLiteDataSource()
  source.url = "jdbc:sqlite:$filepath"
  val db = Database(source)
  if (!File(filepath).exists())
    initializeDatabase(db, initialPersistence())

  return db
}

//fun createVictory(db: Database, victory: Victory) {
//  val sql = """
//    INSERT INTO victories (player) VALUES ('${victory.player}')
//  """.trimIndent()
//  executeSql(db, sql)
//}
//
//fun queryStats(db: Database, player: String): PlayerStats {
//  val sql = """
//    SELECT
//      (SELECT COUNT(*) FROM victories WHERE player = $player) as victoryCount
//  """.trimIndent()
//  return querySql(db, sql) {
//    PlayerStats(
//        player = player,
//        victoryCount = it.getInt(0)
//    )
//  }
//}

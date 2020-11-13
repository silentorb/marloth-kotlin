package persistence

import org.sqlite.SQLiteDataSource
import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.LooseGraph
import silentorb.mythic.ent.PropertySchema
import java.io.File
import java.sql.PreparedStatement
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

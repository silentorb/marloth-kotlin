package persistence

import org.sqlite.SQLiteDataSource
import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.LooseGraph
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

fun <T> querySql(db: Database, sql: String, arguments: List<String>, handler: (ResultSet) -> T): T {
  val connection = db.source.connection
  return connection.prepareStatement(sql).use { statement ->
    var step = 1
    for (argument in arguments) {
      statement.setString(step++, argument)
    }
    handler(statement.executeQuery())
  }
}

fun executeSql(db: Database, sql: String) {
  val connection = db.source.connection
  connection.createStatement().use { statement ->
    statement.execute(sql)
  }
}

fun executeSql(db: Database, sql: String, prepare: (PreparedStatement) -> Unit) {
  val connection = db.source.connection
  connection.prepareStatement(sql).use { statement ->
    prepare(statement)
    statement.execute()
  }
}

fun executeSqlWithStringArguments(db: Database, sql: String, arguments: List<String>) {
  executeSql(db, sql) { statement ->
    var step = 1
    for (argument in arguments) {
      statement.setString(step++, argument)
    }
  }
}

fun executeSqlWithArguments(db: Database, sql: String, arguments: List<Any>) {
  executeSql(db, sql) { statement ->
    arguments.forEachIndexed { index, argument ->
      val i = index + 1
      when (argument) {
        is String -> statement.setString(i, argument)
        is Int -> statement.setInt(i, argument)
        else -> throw Error("Not supported")
      }
    }
  }
}

fun insertEntry(db: Database, entry: Entry) {
  val (source, property, target) = entry
  val sql = """
INSERT INTO entries (source, property, target) VALUES (?, ?, ?)
ON CONFLICT(source, property, target) DO UPDATE SET touched = date('now')
  """.trimIndent()
  executeSqlWithStringArguments(db, sql, listOf(source, property, target.toString()))
}

fun insertEntries(db: Database, graph: LooseGraph) {
  val valuesClause = graph.joinToString(",\n") { "VALUES (?, ?, ?)" }
  val values = graph.flatMap { listOf(it.source, it.property, it.target.toString()) }
  val sql = """
INSERT INTO entries (source, property, target)
$valuesClause
ON CONFLICT(source, property, target) DO UPDATE SET touched = date('now')
  """.trimIndent()
  executeSqlWithStringArguments(db, sql, values)
}

fun queryEntryValue(db: Database, source: String, property: String): String? {
  val sql = """
SELECT target FROM entries
WHERE source = ? AND property = ?
  """.trimIndent()
  return querySql(db, sql, listOf(source, property)) { result ->
    if (result.next())
      result.getString(1)
    else
      null
  }
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

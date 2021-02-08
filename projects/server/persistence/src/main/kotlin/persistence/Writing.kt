package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import java.sql.PreparedStatement

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

fun insertEntry(db: Database, tableName: String, entry: Entry) {
  val (source, property, target) = entry
  val sql = """
INSERT INTO $tableName (source, property, target) VALUES (?, ?, ?)
ON CONFLICT(source, property, target) DO UPDATE SET touched = date('now')
  """.trimIndent()
  executeSqlWithStringArguments(db, sql, listOf(source, property, target.toString()))
}

fun insertEntries(db: Database, tableName: String, graph: Graph) {
  val valuesClause = graph.joinToString(",\n") { "VALUES (?, ?, ?)" }
  val values = graph.flatMap { listOf(it.source, it.property, it.target.toString()) }
  val sql = """
INSERT INTO $tableName (source, property, target)
$valuesClause
ON CONFLICT(source, property, target) DO UPDATE SET touched = date('now')
  """.trimIndent()
  executeSqlWithStringArguments(db, sql, values)
}

package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.LooseGraph
import silentorb.mythic.ent.PropertySchema
import java.sql.ResultSet

fun <T> querySql(db: Database, sql: String, handler: (ResultSet) -> T): T {
  val connection = db.source.connection
  return connection.prepareStatement(sql).use { statement ->
    handler(statement.executeQuery())
  }
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

fun queryEntryValue(db: Database, tableName: String, source: String, property: String): String? {
  val sql = """
SELECT target FROM $tableName
WHERE source = ? AND property = ?
  """.trimIndent()
  return querySql(db, sql, listOf(source, property)) { result ->
    if (result.next())
      result.getString(1)
    else
      null
  }
}

fun readEntryRecord(properties: PropertySchema, result: ResultSet): Entry {
  val source = result.getString(1)
  val property = result.getString(2)
  val propertyInfo = properties[property]
      ?: throw Error("Unknown database property type: $property")

  val target = when (propertyInfo.type) {
    String -> result.getString(3)
    Int -> result.getInt(3)
    else -> throw Error("Unsupported database property type")
  }

  return Entry(source, property, target)
}

fun queryEntries(db: Database, table: Table): LooseGraph {
  val tableName = table.name
  val properties = table.properties

  val sql = """
SELECT source, property, target FROM $tableName
  """.trimIndent()
  return querySql(db, sql) { result ->
    val entries: MutableList<Entry> = mutableListOf()
    while (result.next()) {
      val entry = readEntryRecord(properties, result)
      entries.add(entry)
    }

    entries
  }
}

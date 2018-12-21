package persistence

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import simulation.PlayerStats
import simulation.Victory
import java.sql.ResultSet

// Thin wrapper for later additional features like being able to toggle global SQL logging
fun <T> t(db: Database? = null, statement: Transaction.() -> T): T = transaction(db, statement)

//object PlayerStatsTable : Table("player_stats") {
//  val name = varchar("name", 255).primaryKey()
//  val created = datetime("created").defaultExpression(CurrentDateTime())
//  val modified = datetime("modified").defaultExpression(CurrentDateTime())
//
//  val winCount = integer("winCount").default(0)
//}

fun <T> rawQuery(db: Database, sql: String, handler: (ResultSet) -> T): T {
  val connection = TransactionManager.current().connection
  val statement = connection.createStatement()
  return handler(statement.executeQuery(sql))
}

object VictoryTable : Table("victories") {
  val id = integer("id").primaryKey().autoIncrement()
  val player = varchar("player", 255)
  val date = datetime("date").defaultExpression(CurrentDateTime())
}

fun createVictory(db: Database, victory: Victory) {
  t(db) {
    VictoryTable.insert {
      it[player] = victory.player
    }
  }
}

fun queryStats(db: Database, player: String): PlayerStats {
  val sql = """
    SELECT
      (SELECT COUNT(*) FROM victories WHERE player = $player) as victoryCount
  """.trimIndent()
  return rawQuery<PlayerStats>(db, sql) {
    PlayerStats(
        player = player,
        victoryCount = it.getInt(0)
    )
  }
}
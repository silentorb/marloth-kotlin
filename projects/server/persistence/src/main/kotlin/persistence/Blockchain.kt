package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

typealias Hash = Int

object ChangeActions {
  const val add = 1
  const val remove = 2
}

data class Block(
    val additions: Hash,
    val previous: Hash?,
    val removals: Hash,
    val timestamp: Int,
)

fun insertBlock(db: Database, previous: ByteArray, additions: Graph, removals: Graph) {
  val timestamp = System.currentTimeMillis()
  val additionsHash = hashGraph(additions)
  val removalsHash = hashGraph(removals)
  val hash = additionsHash + previous + removalsHash + valueToBytes(timestamp)

  val sql = """
INSERT INTO blocks (hash, additions, previous, removals, timestamp) VALUES (?, ?, ?, ?, ?);
"""
  executeSqlWithArguments(db, sql, listOf(
      hashToString(hash),
      hashToString(additionsHash),
      hashToString(previous),
      hashToString(removalsHash),
      timestamp,
  ))
}

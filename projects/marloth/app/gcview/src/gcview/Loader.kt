package gcview

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

val decimalPattern = Regex("\\d+\\.\\d+")

fun getDecimals(line: String) =
    decimalPattern.findAll(line).map { it.value.toFloat() }.toList()

fun loadCollection(lines: Collection<String>): GarbageCollection {
  fun lineIndex(pattern: String) =
      lines.indexOfFirst { it.contains(pattern) }

  fun findLine(pattern: String) =
      lines.first { it.contains(pattern) }

  fun decimals(linePattern: String): List<Float> =
      getDecimals(findLine(linePattern))

  val startTime = getDecimals(lines.first()).first()
  val totalEvacuationTime = decimals("  Evacuate Collection Set")[1]

  return GarbageCollection(
      startTime = startTime,
      evacuateCollectionSet = EvacuateCollectionSet(
          duration = totalEvacuationTime
      )
  )
}

fun loadLogFile(file: File): LogData {
  val lines = Files.readAllLines(file.toPath())
  val collections = mutableListOf<GarbageCollection>()
  var blockStart = -1
  for (i in 0 until lines.size) {
    val line = lines[i]
    if (blockStart == -1) {
      if (line.contains("Pause Young (G1 Evacuation Pause)")) {
        blockStart = i
      }
    } else {
      if (line.contains("Heap after GC invocations")) {
        collections.add(loadCollection(lines.subList(blockStart, i)))
        blockStart = -1
      }
    }
  }
  return LogData(
      collections = collections.toList()
  )
}
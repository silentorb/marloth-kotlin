package persistence

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest


fun hashBytes(value: ByteArray): ByteArray {
  val md = MessageDigest.getInstance("SHA-1")
  return md.digest(value)
}

fun hashToString(digest: ByteArray): String {
  val number = BigInteger(1, digest)
  return number.toString(16)
}

fun hashBytesToString(value: ByteArray): String {
  val digest = hashBytes(value)
  return hashToString(digest)
}

// TODO: This value may be wrong.  Instead of figuring it out ahead of time I'm just waiting until I can debug the hashing output.
const val hashByteLength = 128

fun valueToBytes(value: Any): ByteArray =
    when (value) {
      is String -> ByteBuffer.allocate(2).putShort(value.length.toShort()).array() +
          value.toByteArray(StandardCharsets.UTF_8)

      is Int -> ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value).array()

      is Long -> ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()

      else -> throw Error("Not supported")
    }

fun hashEntry(entry: Entry): ByteArray {
  val (source, property, target) = entry
  return valueToBytes(source) + valueToBytes(property) + valueToBytes(target)
}

fun hashStringToBytes(value: String?): ByteArray =
    value?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(hashByteLength) { 0 }

fun hashSort(a: ByteArray, b: ByteArray): Int {
  for (i in 0 until hashByteLength) {
    if (a[i] > b[i])
      return 1

    if (a[i] < b[i])
      return -1
  }
  return 0
}

fun hashGraph(graph: Graph): ByteArray {
  val hashes = graph
      .map(::hashEntry)
      .sortedWith(::hashSort)

  val buffer = ByteBuffer.allocate(hashes.size * hashByteLength)
  for (hash in hashes) {
    buffer.put(hash)
  }
  return hashBytes(buffer.array())
}

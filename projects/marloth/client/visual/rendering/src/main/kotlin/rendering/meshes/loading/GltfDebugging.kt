package rendering.meshes.loading

import java.io.File
import java.io.PrintWriter
import java.nio.ByteBuffer

private fun getValue(buffer: ByteBuffer, componentType: Int): Any {
  return when (componentType) {
    ComponentType.UnsignedByte.value -> buffer.get().toInt() and 0xFF
    ComponentType.UnsignedShort.value -> buffer.getShort().toInt() and 0xFF
    ComponentType.UnsignedInt.value -> buffer.getInt() and 0xFF
    ComponentType.Float.value -> buffer.getFloat()
    else -> throw Error("Not implemented.")
  }
}

private val componentCountMap = mapOf(
    AccessorType.SCALAR to 1,
    AccessorType.VEC2 to 2,
    AccessorType.VEC2 to 2,
    AccessorType.VEC3 to 3,
    AccessorType.VEC4 to 4,
    AccessorType.MAT4 to 16
)

private fun log(out: PrintWriter, text: Any, position: Int) {
  val line = " " + position.toString().padStart(7, ' ') + " " + text
  out.println(line)
//    println(line)
}

fun logBuffer(buffer: ByteBuffer, info: GltfInfo) {
  File("gltf-data.txt").printWriter().use { out ->
    info.accessors.forEachIndexed { index, accessor ->
      val bufferView = info.bufferViews[accessor.bufferView]
      out.println("Accessor " + index + " " + accessor.name)

      for (i in 0 until accessor.count) {
        buffer.position(bufferView.byteOffset + accessor.byteOffset + i * bufferView.byteStride)
        val componentCount = componentCountMap[accessor.type]!!
        val position = buffer.position()
        val values = (0 until componentCount).map {
          getValue(buffer, accessor.componentType).toString()
        }
        val valueString = values.joinToString(", ")
        log(out, valueString, position)
      }
    }
    /*
      when (accessor.componentType) {

        ComponentType.UnsignedByte.value -> {
          val intBuffer = buffer
          for (i in 0 until accessor.count) {
            val position = buffer.position()
            val value = intBuffer.get().toInt() and 0xFF
            log(value, position)
          }
        }

        ComponentType.UnsignedShort.value -> {
          val intBuffer = buffer.asShortBuffer()
          for (i in 0 until accessor.count) {
            val position = buffer.position()
            val value = intBuffer.get()
            val value2 = value.toInt() and 0xFF
            log("" + value + " " + value2, position)
          }
        }

        ComponentType.UnsignedInt.value -> {
          for (i in 0 until accessor.count) {
            val position = buffer.position()
            val value = buffer.get().toInt() and 0xFF
            log(value, position)
          }
        }

        ComponentType.Float.value -> {
          when (accessor.type) {

            AccessorType.SCALAR -> {
              for (i in 0 until accessor.count) {
                val position = buffer.position()
                val value1 = buffer.getFloat()
                log("" + value1, position)
              }
            }

            AccessorType.VEC2 -> {
              for (i in 0 until accessor.count) {
                val position = buffer.position()
                val value1 = buffer.getFloat()
                val value2 = buffer.getFloat()
                log("" + value1 + ", " + value2, position)
              }
            }

            AccessorType.VEC3 -> {
              for (i in 0 until accessor.count) {
                val position = buffer.position()
                val value1 = buffer.getFloat()
                val value2 = buffer.getFloat()
                val value3 = buffer.getFloat()
                log("" + value1 + ", " + value2 + ", " + value3, position)
              }
            }

            AccessorType.VEC4 -> {
              for (i in 0 until accessor.count) {
                val position = buffer.position()
                val value1 = buffer.getFloat()
                val value2 = buffer.getFloat()
                val value3 = buffer.getFloat()
                val value4 = buffer.getFloat()
                log("" + value1 + ", " + value2 + ", " + value3 + ", " + value4, position)
              }
            }

            AccessorType.MAT4 -> {
              val position = buffer.position()
              log("Matrix...", position)
            }

            else -> throw Error("Not implemented.")
          }
        }

        else ->
          throw Error("Not implemented")
      }
    }
    */
  }
}

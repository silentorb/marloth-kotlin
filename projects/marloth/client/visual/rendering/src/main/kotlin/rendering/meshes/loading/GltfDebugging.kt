package rendering.meshes.loading

import java.nio.ByteBuffer

fun logBuffer(buffer: ByteBuffer, info: GltfInfo) {
  for (accessor in info.accessors) {
    val view = info.bufferViews[accessor.bufferView]
    buffer.position(view.byteOffset)
    println("Buffer View " + accessor.bufferView)
    when (accessor.componentType) {

      ComponentType.UnsignedByte.value -> {
        val intBuffer = buffer
        for (i in 0 until accessor.count) {
          val value = intBuffer.get().toInt() and 0xFF
          println(value)
        }
      }

      ComponentType.UnsignedShort.value -> {
        val intBuffer = buffer.asShortBuffer()
        for (i in 0 until accessor.count) {
          val value = intBuffer.get()
          val value2 = value.toInt() and 0xFF
          println("" + value + " " + value2)
        }
      }

      ComponentType.UnsignedInt.value -> {
        for (i in 0 until accessor.count) {
          val value = buffer.get().toInt() and 0xFF
          println(value)
        }
      }

      ComponentType.Float.value -> {
        when (accessor.type) {

          AccessorType.SCALAR -> {
            for (i in 0 until accessor.count) {
              val value1 = buffer.getFloat()
              println("" + value1)
            }
          }

          AccessorType.VEC2 -> {
            for (i in 0 until accessor.count) {
              val value1 = buffer.getFloat()
              val value2 = buffer.getFloat()
              println("" + value1 + ", " + value2)
            }
          }

          AccessorType.VEC3 -> {
            for (i in 0 until accessor.count) {
              val value1 = buffer.getFloat()
              val value2 = buffer.getFloat()
              val value3 = buffer.getFloat()
              println("" + value1 + ", " + value2 + ", " + value3)
            }
          }

          AccessorType.VEC4 -> {
            for (i in 0 until accessor.count) {
              val value1 = buffer.getFloat()
              val value2 = buffer.getFloat()
              val value3 = buffer.getFloat()
              val value4 = buffer.getFloat()
              println("" + value1 + ", " + value2 + ", " + value3 + ", " + value4)
            }
          }

          AccessorType.MAT4 -> {
            println("Matrix...")
          }

          else -> throw Error("Not implemented.")
        }
      }

      else ->
        throw Error("Not implemented")
    }
  }

}

package rendering

import mythic.glowing.SimpleTriangleMesh
import org.lwjgl.BufferUtils
import java.io.DataInputStream
import java.io.InputStream

private fun loadResource(name: String): InputStream {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResourceAsStream(name)
}

fun readInt(dataStream: DataInputStream) = Integer.reverseBytes(dataStream.readInt())// and 0xFFFFFFFFL.toInt()
fun readFloat(dataStream: DataInputStream) =
    java.lang.Float.intBitsToFloat(Integer.reverseBytes(dataStream.readInt()))

fun loadGltf(vertexSchemas: VertexSchemas, name: String): ModelElements {
  val inputStream = loadResource(name + ".bin")
  val dataStream = DataInputStream(inputStream)

  val triangleCount = 12
  val vertexCount = 24

//  val triangleCount = 6
//  val vertexCount = 12
  val indexCount = triangleCount * 3
  val indices = BufferUtils.createIntBuffer(indexCount)
  val vertices = BufferUtils.createFloatBuffer(3 * 2 * vertexCount)

  dataStream.use {
    //    val magic = readInt(dataStream)
//    if (magic != 0x46546C67)
//      throw Error("Invalid GLTF binary file.")
//
//    val version = readInt(dataStream)
//    val length = readInt(dataStream)
//    while (dataStream.available() > 0) {
//
////    println(x.toString() + ", " + Integer.reverseBytes(x))
//    }

    for (i in 0 until triangleCount) {
      for (x in 0 until 3) {
        val value = dataStream.readByte().toInt()
//        println(value)
        indices.put(value)
      }
    }

    val padding = 4 - indexCount % 4
    for (i in 0 until padding) {
      dataStream.readByte()
    }

    for (i in 0 until vertexCount) {
      for (x in 0 until 3) {
        val value = readFloat(dataStream)
//        val value = dataStream.readFloat()
        println(value)
        vertices.put(value)
      }
      vertices.position(vertices.position() + 3)
    }

    vertices.flip()

    for (i in 0 until vertexCount) {
      vertices.position(vertices.position() + 3)
      for (x in 0 until 3) {
        val value = readFloat(dataStream)
//        val value = dataStream.readFloat()
        println(value)
        vertices.put(value)
      }
    }
  }

  vertices.flip()
  indices.flip()

  return listOf(
      ModelElement(
          mesh = SimpleTriangleMesh(vertexSchemas.imported, vertices, indices),
          material = Material()
      )
  )
}
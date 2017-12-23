import java.nio.ByteBuffer

class MythicInterface {

  external fun initialize()
  external fun update()
  external fun loop()
  external fun shutdown()

  external fun createMesh(meshBuffer: ByteBuffer):Int

  companion object {
    init {
      println("Working Directory = " + System.getProperty("user.dir"))
      println(System.getProperty("java.library.path"))
      System.loadLibrary("libmarloth_java")
    }
  }

}
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

fun getResourceUrl(path: String): URL {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResource(path)
}

fun getResourceStream(name: String): InputStream {
  val classloader = Thread.currentThread().contextClassLoader
  return classloader.getResourceAsStream(name)
}

fun loadTextResource(name: String): String {
  val inputStream = getResourceStream(name)
  val s = Scanner(inputStream).useDelimiter("\\A")
  val result = if (s.hasNext()) s.next() else ""
  return result
}

fun scanTextureResources(rootPath: String): List<String> {
  val modelRoot = getResourceUrl(rootPath)
  val modelRootPath = Paths.get(modelRoot.toURI())
  val pathPrefix = modelRootPath.toString().length - rootPath.length
  val walk = Files.walk(modelRootPath, 10)
  val it = walk.iterator()
  val imageFiles = mutableListOf<String>()
  while (it.hasNext()) {
    val path = it.next()
    val stringPath = path.toString()
    if (stringPath.endsWith(".jpg") || stringPath.endsWith(".png")) {
      imageFiles.add(stringPath.substring(pathPrefix).replace("\\", "/"))
    }
  }

  return imageFiles.toList()
}
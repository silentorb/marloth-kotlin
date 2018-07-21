package lab

import rendering.Renderer
import rendering.createTextureLibrary
import rendering.defaultTextureScale
import java.io.File
import java.net.URI
import java.util.*

enum class WatchedPackage {
  rendering
}

private val watchedPackageFiles = mapOf(
    WatchedPackage.rendering to Renderer::class
)
    .mapValues { it.value.java.getProtectionDomain().getCodeSource().getLocation().toURI() }

val watchList = watchedPackageFiles.map {
  WatchInfo(
      target = it.key,
      uri = it.value,
      lastModified = getLastModified(it.value),
      awaitingRebuildFinish = false
  )
}

data class WatchInfo(
    val target: WatchedPackage,
    val uri: URI,
    var lastModified: Long,
    var awaitingRebuildFinish: Boolean
)

fun getLastModified(uri: URI) =
    File(uri).lastModified()

fun checkForChangedPackages(): List<WatchedPackage> =
    watchList.mapNotNull { info ->
      val lastModified = getLastModified(info.uri)
      val oldValue = info.lastModified

      val now = System.currentTimeMillis()
      if (lastModified > oldValue) {
        println("Package " + info.target + " was modified at " + lastModified + " (gap = " + (lastModified - oldValue) + ")")
        println()
        info.lastModified = lastModified
        info.awaitingRebuildFinish = true
        null
      } else {
        if (info.awaitingRebuildFinish && now > info.lastModified + 1200) {
          println("Package " + info.target + " finished rebuild at " + now  + " (gap = " + (lastModified - oldValue) + ")")
          info.awaitingRebuildFinish = false
          info.target
        }
        else
          null
      }
    }

fun reloadTextures(renderer: Renderer) {
  for (texture in renderer.textures) {
    texture.value.dispose()
  }

  renderer.textures = createTextureLibrary(defaultTextureScale)
}

fun onPackageChanged(app: LabApp, watchedPackage: WatchedPackage) {
  when (watchedPackage) {
    WatchedPackage.rendering -> reloadTextures(app.client.renderer)
  }
}

private var lastWatchedTime = 0L

fun updateWatching(app: LabApp) {
  val changes = checkForChangedPackages()
//  val gap = app.timer.last - lastWatchedTime
//  println(gap)
//  lastWatchedTime = app.timer.last
//  if (gap < 3000)
//    return

  for (change in changes) {
    println("Updating " + change)
    onPackageChanged(app, change)
  }
}
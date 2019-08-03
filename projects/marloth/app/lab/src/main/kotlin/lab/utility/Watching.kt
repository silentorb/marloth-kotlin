package lab.utility

import simulation.misc.Graph
import lab.LabApp
import rendering.*
import java.io.File
import java.net.URI

enum class WatchedPackage {
  generation
}

private val watchedPackageFiles = mapOf(
    WatchedPackage.generation to Graph::class
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
//  for (texture in renderer.mappedTextures) {
//    texture.value.dispose()
//  }
//
//  renderer.mappedTextures = createTextureLibrary(defaultTextureScale)
}

fun reloadMeshes(renderer: Renderer){
//  for (mesh in renderer.meshes.values) {
//    dispose(mesh)
//  }
//
//  renderer.meshes = createMeshes(renderer.vertexSchemas)
}

var shouldReloadWorld = false

fun onPackageChanged(app: LabApp, watchedPackage: WatchedPackage) {
  when (watchedPackage) {
    WatchedPackage.generation -> {
//      reloadTextures(app.gameApp.client.renderer)
//      reloadMeshes(app.gameApp.client.renderer)
      shouldReloadWorld = true
    }
  }
}

fun updateWatching(app: LabApp) {
  val changes = checkForChangedPackages()
  for (change in changes) {
    println("Updating " + change)
    onPackageChanged(app, change)
  }
}

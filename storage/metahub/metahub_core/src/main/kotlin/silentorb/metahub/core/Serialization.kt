package silentorb.metahub.core

import silentorb.mythic.configuration.loadJsonFile
import silentorb.mythic.ent.pipe2
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

fun loadGraph(engine: Engine, stream: InputStream): Graph =
    pipe2(loadJsonFile(stream), listOf(mapValues(engine)))

fun loadGraphFromFile(engine: Engine, path: String): Graph =
    if (File(path).exists())
      loadGraph(engine, FileInputStream(path))
    else
      throw Error("No such file $path")

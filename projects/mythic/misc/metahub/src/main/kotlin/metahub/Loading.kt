package metahub

import configuration.loadJsonFile
import mythic.ent.pipe
import java.io.FileInputStream
import java.io.InputStream

fun loadGraph(engine: Engine, stream: InputStream): Graph =
    pipe(loadJsonFile(stream), listOf(mapValues(engine)))

fun loadGraphFromFile(engine: Engine, path: String): Graph =
    loadGraph(engine, FileInputStream(path))
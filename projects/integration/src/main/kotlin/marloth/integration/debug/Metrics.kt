package marloth.integration.debug

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths

data class MetricSample(
    val metric: String,
    val time: Long,
    val value: Any
)

fun writeMetricSamples(filePath: String, samples: List<MetricSample>) {
  Files.createDirectories(Paths.get(filePath).parent)
  PrintWriter("../logs/metrics.csv").use { out ->
    out.println("Metric,Time,Value")
    for ((metric, time, value) in samples) {
      out.println("$metric,$time,$value")
    }
  }
}

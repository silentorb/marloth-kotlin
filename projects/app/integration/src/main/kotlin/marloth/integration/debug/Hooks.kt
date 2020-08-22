package marloth.integration.debug

import marloth.clienting.rendering.marching.getNanoTime
import marloth.integration.front.GameHooks
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.quartz.nanosecondsToDelta
import simulation.updating.simulationDelta

fun newDebugHooks(): GameHooks? {
  val metricSamples: MutableList<MetricSample> = mutableListOf()
  return GameHooks(

      onTimeStep = { timestep, steps, appState ->
        val minDroppedFrame = getDebugFloat("DROPPED_FRAME_MINIMUM")
        if (minDroppedFrame != null && timestep.rawDelta > minDroppedFrame) {
          println("Dropped frame: ${timestep.rawDelta}")
        }
        if (getDebugBoolean("LOG_PROFILING_METRICS")) {
          val time = timestep.time.latest
          metricSamples.add(MetricSample("Raw Delta", time, timestep.rawDelta))
          metricSamples.add(MetricSample("Delta", time, timestep.delta))
          metricSamples.add(MetricSample("Buffer", time, simulationDelta - timestep.rawDelta))
          for (measurement in appState.client.marching.timeMeasurements) {
            metricSamples.add(MetricSample(measurement.key, time, nanosecondsToDelta(measurement.value)))
          }
        }
      },

      onClose = {
        if (metricSamples.any()) {
          writeMetricSamples("../logs/metrics.csv", metricSamples)
        }
      }
  )
}

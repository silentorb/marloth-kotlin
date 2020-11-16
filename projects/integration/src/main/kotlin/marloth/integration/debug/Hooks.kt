package marloth.integration.debug

import marloth.integration.front.GameHooks
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.editing.renderEditorGui
import simulation.updating.getIdle

fun newDebugHooks(hooks: GameHooks): GameHooks {
  val metricSamples: MutableList<MetricSample> = mutableListOf()
  return hooks.copy(

      onTimeStep = { timestep, steps, appState ->
        val minDroppedFrame = getDebugFloat("DROPPED_FRAME_MINIMUM")
        if (minDroppedFrame != null && timestep.rawDelta > minDroppedFrame) {
          println("Dropped frame: ${timestep.rawDelta}")
        }
        if (getDebugBoolean("LOG_PROFILING_METRICS")) {
          val time = timestep.time.latest
          metricSamples.add(MetricSample("Increment", time, timestep.increment / 1000_000))
          metricSamples.add(MetricSample("Idle", time, getIdle(timestep.increment / 1000_000)))
//          metricSamples.add(MetricSample("Buffer", time, simulationDelta - timestep.rawDelta))
//          for (measurement in appState.client.marching.timeMeasurements) {
//            metricSamples.add(MetricSample(measurement.key, time, nanosecondsToDelta(measurement.value)))
//          }
        }
      },

      onClose = {
        if (metricSamples.any()) {
          writeMetricSamples("../logs/metrics.csv", metricSamples)
        }
      }
  )
}

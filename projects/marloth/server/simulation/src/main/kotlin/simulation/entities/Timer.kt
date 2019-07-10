package simulation.entities

data class Timer(
    val duration: Int
)

val updateTimer: (Timer) -> Timer = { timer ->
  timer.copy(
      duration = timer.duration - 1
  )
}

//val updateTimers: (Table<Timer>) -> Table<Timer> = { timers ->
//  timers
//      .mapValues { updateTimer()(it.value) }
//      .filterValues { it.duration > 0 }
//}

package gcview

data class EvacuateCollectionSet(
    val duration: Float
)

data class GarbageCollection(
    val startTime: Float,
    val evacuateCollectionSet: EvacuateCollectionSet
) {
  val evacuateCollectionSetDuration: Float
    get() = evacuateCollectionSet.duration
}

data class LogData(
    val collections: List<GarbageCollection>
)

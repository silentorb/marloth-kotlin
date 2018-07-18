package gcview

data class GarbageCollection(
    val startTime: Float,
    val youngPauseDuration: Float,
    val evacuateCollectionSetDuration: Float,
    val updateRSMax: Float,
    val objectCopyMax: Float
)

data class LogData(
    val collections: List<GarbageCollection>
)

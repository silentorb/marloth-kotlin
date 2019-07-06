package simulation.evention

data class Trigger(
    val action: Action,
    val dieOnFirstCollision: Boolean = false
)

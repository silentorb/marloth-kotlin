package metahub.metaview.common

import javafx.scene.Node

data class StateTransformChange<T>(
    val previous: T,
    val event: Event
)

data class StateChange<T>(
    val previous: T,
    val next: T,
    val event: Event
)

// Modifies managed state based on events
typealias StateTransformListener<T> = (StateTransformChange<T>) -> (T) -> T

// Modifies external state based on events
typealias SideEffectStateListener<T> = (StateChange<T>) -> Unit

data class View<T>(
    val factory: (T) -> Node,
    val update: StateTransformListener<T>
)
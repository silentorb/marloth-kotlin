package simulation.main

import silentorb.mythic.ent.Id

data class NewHand(
    val components: List<Any>,
    val children: List<NewHand> = listOf(),
    val id: Id? = null
) {
  fun plusComponents(vararg value: Any) =
      this.copy(
          components = components + value
      )

  inline fun <reified T> replaceComponent(transform: (T) -> T): NewHand =
      replaceComponent(this, transform)

  inline fun <reified T> replaceComponent(value: T): NewHand =
      this.copy(
          components = this.components.map { component ->
            if (component is T)
              value!!  // I Don't know why this needs to be cast to not-null
            else
              component
          }
      )
}

data class SimpleHand(
    val id: Id,
    val components: List<Any>,
)

inline fun <reified T> getComponent(hand: NewHand): T? =
    hand.components.filterIsInstance<T>().firstOrNull()

inline fun <reified T> replaceComponent(hand: NewHand, transform: (T) -> T): NewHand =
    hand.copy(
        components = hand.components.map { component ->
          if (component is T)
            transform(component)!!  // I Don't know why this needs to be cast to not-null
          else
            component
        }
    )

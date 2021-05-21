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

  inline fun <reified T> modifyComponent(transform: (T) -> T): NewHand =
      modifyComponent(this, transform)
}

data class SimpleHand(
    val id: Id,
    val components: List<Any>,
)

inline fun <reified T> getComponent(hand: NewHand): T? =
    hand.components.filterIsInstance<T>().firstOrNull()

inline fun <reified T> modifyComponent(hand: NewHand, transform: (T) -> T): NewHand =
    hand.copy(
        components = hand.components.map { component ->
          if (component is T)
            transform(component)!!  // I Don't know why this needs to be cast to not-null
          else
            component
        }
    )

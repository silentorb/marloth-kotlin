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
}

data class SimpleHand(
    val id: Id,
    val components: List<Any>,
)

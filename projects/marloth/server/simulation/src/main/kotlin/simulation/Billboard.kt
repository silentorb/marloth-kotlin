package simulation

data class Billboard(
    val character: Character,
    var text: String,
    val ability: Ability? = null
)
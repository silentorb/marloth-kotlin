package silentorb.mythic.combat

data class DamageDefinition(
    val type: DamageType,
    val amount: Int
)

data class WeaponDefinition(
    val damages: List<DamageDefinition>
)

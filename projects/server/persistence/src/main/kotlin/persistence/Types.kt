package persistence

import silentorb.mythic.ent.PropertySchema

data class Table(
    val name: String,
    val properties: PropertySchema,
)

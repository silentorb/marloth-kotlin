package persistence

import silentorb.mythic.ent.PropertyInfo
import silentorb.mythic.ent.PropertySchema

object PersistenceProperties {
  const val ruler = "ruler"
}

val persistencePropertiesInfo: PropertySchema = mapOf(
    PersistenceProperties.ruler to PropertyInfo(
        type = String,
    )
)

val persistenceTable = Table(
    name = persistenceTableName,
    properties = persistencePropertiesInfo,
)

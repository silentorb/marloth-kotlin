package marloth.integration.misc

import persistence.Table
import marloth.definition.data.persistence.persistencePropertiesInfo
import persistence.persistenceTableName

val persistenceTable = Table(
    name = persistenceTableName,
    properties = persistencePropertiesInfo,
)

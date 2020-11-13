package persistence

const val databaseFormatVersion: Int = 1
const val persistenceTableName = "persistence"

fun schemaSql() = listOf(

    """
CREATE TABLE database_info (
  format_version INT NOT NULL
);
""",

    """
CREATE TABLE active_game (
  source TEXT NOT NULL,
  property TEXT NOT NULL,
  target TEXT NOT NULL,
  created DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  touched DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  PRIMARY KEY (source, property, target)
);
""",

    """
CREATE TABLE $persistenceTableName (
  source TEXT NOT NULL,
  property TEXT NOT NULL,
  target TEXT NOT NULL,
  created DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  touched DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  PRIMARY KEY (source, property, target)
);
""",

    """
CREATE TABLE blocks (
  hash INTEGER NOT NULL,
  additions INTEGER NOT NULL,
  previous INTEGER,
  removals INTEGER NOT NULL,
  timestamp DATETIME NOT NULL,
  PRIMARY KEY (hash)
);
""",

    """
CREATE TABLE change_sets (
  hash INTEGER NOT NULL,
  entry INTEGER NOT NULL,
  action INTEGER NOT NULL,  
  PRIMARY KEY (hash)
);
""",

    """
CREATE TABLE hash_entries (
  hash INTEGER NOT NULL,
  source TEXT NOT NULL,
  property TEXT NOT NULL,
  target TEXT NOT NULL,
  PRIMARY KEY (hash)
);
"""
)

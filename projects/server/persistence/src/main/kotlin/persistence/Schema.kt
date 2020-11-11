package persistence

fun schemaSql() =
"""
CREATE TABLE entries (
  source TEXT NOT NULL,
  property TEXT NOT NULL,
  target TEXT NOT NULL,
  created DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  touched DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
  PRIMARY KEY (source, property, target)
);
"""

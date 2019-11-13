package generation.architecture.building

import generation.next.Builder

fun compose(vararg builders: Builder): Builder = { input ->
  builders.flatMap { it(input) }
}

package groovier

import groovy.lang.GroovyShell
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

fun createGroovyShell(staticClasses: List<String>, aliases: Map<String, String>, wildcards: List<String>): GroovyShell {
  val customizer = ImportCustomizer()
  customizer.addStaticStars(*staticClasses.toTypedArray())
  for (alias in aliases) {
    customizer.addImport(alias.value, alias.key)
  }
  customizer.addStarImports(*wildcards.toTypedArray())
  val configuration = CompilerConfiguration()
  configuration.addCompilationCustomizers(customizer)
  return GroovyShell(Thread.currentThread().getContextClassLoader(), configuration)
}
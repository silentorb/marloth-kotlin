package metaview.views

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.control.ToolBar
import metaview.*

fun toolBarView(state: State, emit: Emitter): Node {
  val domainSelect = ComboBox<Domain>()
  domainSelect.items.addAll(Domain.modeling, Domain.texturing)
  domainSelect.value = state.domain
  domainSelect.valueProperty().addListener { event ->
    emit(Event(EventType.switchDomain, domainSelect.value))
  }
  val toolbar = ToolBar(
      domainSelect
  )
  return toolbar
}
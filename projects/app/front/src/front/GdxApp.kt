package front

import clienting.Client
import com.badlogic.gdx.ApplicationAdapter

class GdxApp : ApplicationAdapter() {
  private var client: Client? = null

  override fun create() {
    client = Client()
  }

  override fun render() {
    val localClient = client
    if (localClient != null)
      localClient.update()
  }

  override fun dispose() {
  }
}

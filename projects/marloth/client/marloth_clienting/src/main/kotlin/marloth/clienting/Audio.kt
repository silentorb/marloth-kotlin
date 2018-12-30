package marloth.clienting

import mythic.aura.AudioState
import mythic.aura.updateSounds

fun updateAudioState(client: Client, delta: Float): (AudioState) -> AudioState = { state ->
  val newSounds = updateSounds(client.platform.audio, client.soundLibrary, delta)(state.sounds)
  state.copy(
      sounds = newSounds
  )
}

fun updateClientStateAudio(client: Client, delta: Float): (ClientState) -> ClientState = { state ->
  val newAudio = updateAudioState(client, delta)(state.audio)
  state.copy(
      audio = newAudio
  )
}
package marloth.clienting

import mythic.aura.SoundLibrary
import mythic.aura.SoundTable
import mythic.aura.updateSounds

data class AudioState(
    val sounds: SoundTable
)

fun newAudioState() =
    AudioState(
        sounds = mapOf()
    )

fun updateAudioState(client: Client): (AudioState) -> AudioState = { state ->
  val newSounds = updateSounds(client.platform.audio, client.soundLibrary)(state.sounds)
  state.copy(
      sounds = newSounds
  )
}

fun updateClientStateAudio(client: Client): (ClientState) -> ClientState = { state ->
  val newAudio = updateAudioState(client)(state.audio)
  state.copy(
      audio = newAudio
  )
}
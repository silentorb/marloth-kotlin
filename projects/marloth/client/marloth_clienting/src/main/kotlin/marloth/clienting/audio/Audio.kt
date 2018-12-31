package marloth.clienting.audio

import getResourceUrl
import marloth.clienting.Client
import marloth.clienting.ClientState
import mythic.aura.*
import mythic.ent.Id
import mythic.ent.entityMap
import mythic.ent.newIdSource
import mythic.platforming.PlatformAudio
import mythic.spatial.Vector3
import scenery.Sounds
import simulation.World
import java.nio.ShortBuffer

fun updateAudioStateSounds(client: Client, listenerPosition: Vector3?): (AudioState) -> AudioState = { state ->
  val samples = client.platform.audio.availableBuffer / 4
  val newSounds = if (samples > 0)
    updateSounds(client.platform.audio, client.soundLibrary, samples, listenerPosition)(state.sounds)
  else
    state.sounds

  state.copy(
      sounds = newSounds
  )
}

fun updateClientStateAudio(client: Client, listenerPosition: Vector3?): (ClientState) -> ClientState = { state ->
  val newAudio = updateAudioStateSounds(client, listenerPosition)(state.audio)
  state.copy(
      audio = newAudio
  )
}

fun newClientStateSounds(previous: ClientState, worlds: List<World>): (ClientState) -> ClientState = { state ->
  val nextId = newIdSource(state.audio.nextSoundId)
  val newSounds = newClientSounds(nextId, previous, state)
      .plus(entityMap(newGameSounds(nextId, worlds)))

  state.copy(
      audio = state.audio.copy(
          sounds = state.audio.sounds.plus(newSounds),
          nextSoundId = nextId()
      )
  )
}

fun loadAudioResource(audio: PlatformAudio, name: String): ShortBuffer =
    audio.loadSound(getResourceUrl("audio/$name.ogg").file.drop(1))

fun loadSounds(audio: PlatformAudio): Map<Id, SoundData> =
    Sounds.values().mapIndexed { i, entry ->
      val buffer = loadAudioResource(audio, entry.name)
      val sound = SoundData(
          id = i.toLong(),
          buffer = buffer,
          duration = buffer.limit().toLong()
      )
      Pair(sound.id, sound)
    }
        .associate { it }

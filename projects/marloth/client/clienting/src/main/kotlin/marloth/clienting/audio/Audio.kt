package marloth.clienting.audio

import silentorb.mythic.lookinglass.getResourceUrl
import marloth.clienting.Client
import marloth.clienting.ClientState
import marloth.clienting.getListenerPosition
import silentorb.mythic.ent.*
import silentorb.mythic.platforming.PlatformAudio
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.Sounds
import silentorb.mythic.aura.*
import simulation.main.World

const val maxSoundDistance: Float = 30f

data class AudioConfig(
    var soundVolume: Float = 0.75f
)

fun updateAudioStateSounds(client: Client, previousSounds: Table<Sound>, nextSounds: Table<Sound>, listenerPosition: Vector3?, volume: Float): (AudioState) -> AudioState = { state ->
  //  val samples = client.platform.audio.availableBuffer / 4
  val newSounds = nextSounds.filterKeys { !previousSounds.keys.contains(it) }
      .filterValues { (it.position == null && listenerPosition == null) || it.position!!.distance(listenerPosition!!) < maxSoundDistance }
  val sounds = updateSoundPlaying(client.platform.audio, newSounds, client.soundLibrary, listenerPosition, volume)(state.sounds)

  state.copy(
      sounds = sounds
  )
}

fun updateClientStateAudio(client: Client, previousSounds: Table<Sound>, nextSounds: Table<Sound>,
                           listenerPosition: Vector3?): (ClientState) -> ClientState = { state ->
  val audio = updateAudioStateSounds(client, previousSounds, nextSounds, listenerPosition, state.audio.volume)(state.audio)
  state.copy(
      audio = audio
  )
}

fun loadAudioResource(audio: PlatformAudio, name: String) =
    audio.loadSound(getResourceUrl("audio/$name.ogg")!!.file.drop(1))

fun loadSounds(audio: PlatformAudio): SoundLibrary =
    Sounds.values().mapIndexed { i, entry ->
      val (buffer, duration) = loadAudioResource(audio, entry.name)
      val sound = SoundData(
          type = entry.name,
          buffer = buffer,
          duration = duration
      )
      Pair(sound.type, sound)
    }
        .associate { it }

fun updateClientAudio(previous: ClientState, client: Client, worlds: List<World>): (ClientState) -> ClientState =
    pipe(
        updateClientStateAudio(client, worlds.first().deck.sounds, worlds.last().deck.sounds, getListenerPosition(worlds.last().deck))
    )

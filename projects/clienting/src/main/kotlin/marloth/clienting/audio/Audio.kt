package marloth.clienting.audio

import marloth.clienting.Client
import marloth.clienting.getListenerPosition
import silentorb.mythic.ent.*
import silentorb.mythic.platforming.PlatformAudio
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.SoundId
import silentorb.mythic.aura.*
import simulation.main.World

const val maxSoundDistance: Float = 30f

data class AudioConfig(
    var soundVolume: Float = 0.75f
)

fun updateAudioStateSounds(client: Client, previousSounds: Table<Sound>, nextSounds: Table<Sound>, listenerPosition: Vector3?): (AudioState) -> AudioState = { state ->
  //  val samples = client.platform.audio.availableBuffer / 4
  val newSounds = nextSounds.filterKeys { !previousSounds.keys.contains(it) }
      .filterValues { (it.position == null && listenerPosition == null) || it.position!!.distance(listenerPosition!!) < maxSoundDistance }
  val sounds = updateSoundPlaying(client.platform.audio, newSounds, client.soundLibrary, listenerPosition, state.volume)(state.sounds)

  state.copy(
      sounds = sounds
  )
}

fun loadAudioResource(audio: PlatformAudio, name: String) =
    audio.loadSound("audio/$name.ogg")
//audio.loadSound(getResourceUrl("audio/$name.ogg")!!.file.drop(1))

fun loadSounds(audio: PlatformAudio): SoundLibrary =
    reflectProperties<String>(SoundId).mapIndexed { i, entry ->
      val (buffer, duration) = loadAudioResource(audio, entry)
      val sound = SoundData(
          type = entry,
          buffer = buffer,
          duration = duration
      )
      Pair(sound.type, sound)
    }
        .associate { it }

fun updateClientAudio(client: Client, worlds: List<World>, audioState: AudioState) =
    if (worlds.size < 2)
      audioState
    else
      updateAudioStateSounds(client, worlds.first().deck.sounds, worlds.last().deck.sounds, getListenerPosition(worlds.last().deck))(audioState)
